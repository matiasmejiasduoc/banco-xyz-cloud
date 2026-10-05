package cl.duoc.bancoxyz.bff.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cl.duoc.bancoxyz.bff.web.api.Agregador;
import cl.duoc.bancoxyz.bff.web.api.DetalleCuentaWeb;
import cl.duoc.bancoxyz.bff.web.api.PanelWeb;
import cl.duoc.bancoxyz.bff.web.api.TransformadorWeb;
import cl.duoc.bancoxyz.bff.web.api.VistaWebService;
import cl.duoc.bancoxyz.bff.web.cliente.CuentaOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.CuentasCliente;
import cl.duoc.bancoxyz.bff.web.cliente.EstadisticasCuentasOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.MovimientoOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.MovimientosCliente;
import cl.duoc.bancoxyz.bff.web.cliente.ResumenDiarioOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.ResumenMovimientosOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.TransaccionesCliente;
import cl.duoc.bancoxyz.commons.api.Pagina;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

class VistaWebServiceTest {

    private static final LocalDate DESDE = LocalDate.of(2024, 1, 1);
    private static final LocalDate HASTA = LocalDate.of(2024, 1, 31);

    private final CuentasCliente cuentas = mock(CuentasCliente.class);
    private final MovimientosCliente movimientos = mock(MovimientosCliente.class);
    private final TransaccionesCliente transacciones = mock(TransaccionesCliente.class);
    private final ExecutorService ejecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final VistaWebService servicio = new VistaWebService(cuentas, movimientos, transacciones,
            new TransformadorWeb(), new Agregador(ejecutor), 20);

    @AfterEach
    void cerrar() {
        ejecutor.close();
    }

    @Test
    void elDetalleAgregaCuentaResumenYMovimientosConDatosCalculados() {
        when(cuentas.obtener(106L)).thenReturn(cuenta());
        when(movimientos.resumen(106L, null, null, null)).thenReturn(resumen());
        when(movimientos.listar(eq(106L), isNull(), isNull(), isNull(), eq(0), eq(20))).thenReturn(
                Pagina.de(List.of(new MovimientoOrigen(1L, 106L, DESDE, "RETIRO", new BigDecimal("1500.00"),
                        "Retiro parcial", "LEGACY", null, null, LocalDateTime.now())), 0, 20, 1));

        DetalleCuentaWeb detalle = servicio.detalleCuenta(106L, null);

        assertThat(detalle.cuenta().saldoProyectado()).isEqualByComparingTo("5125");
        assertThat(detalle.cuenta().tipoDescripcion()).isEqualTo("Cuenta de ahorro");
        assertThat(detalle.ultimosMovimientos().getFirst().montoConSigno()).isEqualByComparingTo("-1500");
        assertThat(detalle.resumen().distribucion()).extracting(d -> d.porcentajeDelMonto())
                .containsExactly(new BigDecimal("75.00"), new BigDecimal("25.00"));
    }

    @Test
    void elDetalleDeUnaCuentaInexistentePropagaElError() {
        when(cuentas.obtener(999L)).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                null, null, null));
        when(movimientos.resumen(any(), any(), any(), any())).thenReturn(resumen());
        when(movimientos.listar(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Pagina.de(List.of(), 0, 20, 0));

        assertThatThrownBy(() -> servicio.detalleCuenta(999L, null)).isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void elPanelRespondeConLasSeccionesDisponiblesSiUnServicioFalla() {
        when(cuentas.estadisticas())
                .thenReturn(new EstadisticasCuentasOrigen(50, new BigDecimal("424000"), List.of()));
        when(transacciones.resumenDiario(DESDE, HASTA)).thenReturn(List.of(
                new ResumenDiarioOrigen(DESDE, 3, new BigDecimal("800"), new BigDecimal("200"), 1)));
        when(movimientos.resumen(null, null, DESDE, HASTA)).thenThrow(new ResourceAccessException("sin conexion"));

        PanelWeb panel = servicio.panel(DESDE, HASTA);

        assertThat(panel.cuentas().totalCuentas()).isEqualTo(50);
        assertThat(panel.transacciones().balance()).isEqualByComparingTo("600");
        assertThat(panel.movimientos()).isNull();
        assertThat(panel.avisos()).containsExactly("La seccion movimientos no esta disponible en este momento");
    }

    private CuentaOrigen cuenta() {
        return new CuentaOrigen(106L, "Diana Prince", 35, "AHORRO", new BigDecimal("5000.00"),
                new BigDecimal("0.025"), new BigDecimal("125.00"), null, LocalDateTime.now());
    }

    private ResumenMovimientosOrigen resumen() {
        return new ResumenMovimientosOrigen(106L, null, null, 2, new BigDecimal("3000.00"),
                new BigDecimal("1000.00"), new BigDecimal("2000.00"), List.of(
                        new ResumenMovimientosOrigen.PorTipo("DEPOSITO", 1, new BigDecimal("3000.00")),
                        new ResumenMovimientosOrigen.PorTipo("RETIRO", 1, new BigDecimal("1000.00"))),
                DESDE, HASTA);
    }
}
