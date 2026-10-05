package cl.duoc.bancoxyz.bff.cajero;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.bancoxyz.bff.cajero.api.CajeroService;
import cl.duoc.bancoxyz.bff.cajero.api.ComprobanteRetiro;
import cl.duoc.bancoxyz.bff.cajero.api.OperacionCajeroException;
import cl.duoc.bancoxyz.bff.cajero.cliente.CuentaOrigen;
import cl.duoc.bancoxyz.bff.cajero.cliente.CuentasCliente;
import cl.duoc.bancoxyz.bff.cajero.cliente.OperacionOrigen;
import cl.duoc.bancoxyz.bff.cajero.config.CajeroProperties;
import cl.duoc.bancoxyz.bff.cajero.evento.PublicacionFallidaException;
import cl.duoc.bancoxyz.bff.cajero.evento.PublicadorRetiros;
import cl.duoc.bancoxyz.commons.evento.RetiroRealizado;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CajeroServiceTest {

    private final CuentasCliente cuentas = mock(CuentasCliente.class);
    private final PublicadorRetiros publicador = mock(PublicadorRetiros.class);
    private final CajeroService servicio = new CajeroService(cuentas, publicador, new CajeroProperties(
            new CajeroProperties.Retiro(new BigDecimal("1000"), new BigDecimal("10000"), new BigDecimal("1000")),
            Duration.ofSeconds(5)));

    @Test
    void retiroExitosoDebitaPublicaElEventoYEnmascaraLaCuenta() {
        BigDecimal monto = new BigDecimal("2000");
        when(cuentas.debitar(101L, monto, "CAJ-001-clave-0001")).thenReturn(
                new OperacionOrigen("CAJ-001-clave-0001", 101L, monto, new BigDecimal("5000"), LocalDateTime.now()));

        ComprobanteRetiro comprobante = servicio.retirar(101L, monto, "CAJ-001", "clave-0001");

        ArgumentCaptor<RetiroRealizado> evento = ArgumentCaptor.forClass(RetiroRealizado.class);
        verify(publicador).publicar(evento.capture());
        assertThat(evento.getValue().referencia()).isEqualTo("CAJ-001-clave-0001");
        assertThat(evento.getValue().terminal()).isEqualTo("CAJ-001");
        assertThat(evento.getValue().canal()).isEqualTo("CAJERO");
        assertThat(comprobante.cuenta()).isEqualTo("****01");
        assertThat(comprobante.saldoDisponible()).isEqualByComparingTo("5000");
    }

    @Test
    void siNoSePuedePublicarElEventoSeRevierteElDebito() {
        BigDecimal monto = new BigDecimal("3000");
        when(cuentas.debitar(eq(101L), eq(monto), anyString())).thenReturn(
                new OperacionOrigen("ref", 101L, monto, new BigDecimal("2000"), LocalDateTime.now()));
        doThrow(new PublicacionFallidaException("ref", new IllegalStateException("kafka caido")))
                .when(publicador).publicar(any());

        assertThatThrownBy(() -> servicio.retirar(101L, monto, "CAJ-001", "clave-0002"))
                .isInstanceOf(OperacionCajeroException.class)
                .hasFieldOrPropertyWithValue("codigo", "OPERACION_REVERTIDA");
        verify(cuentas).revertirDebito(101L, "CAJ-001-clave-0002");
    }

    @Test
    void rechazaMontosQueNoSonMultiplosOEstanFueraDeRango() {
        assertThatThrownBy(() -> servicio.retirar(101L, new BigDecimal("1500"), "CAJ-001", "clave-0003"))
                .hasFieldOrPropertyWithValue("codigo", "MONTO_INVALIDO");
        assertThatThrownBy(() -> servicio.retirar(101L, new BigDecimal("20000"), "CAJ-001", "clave-0004"))
                .hasFieldOrPropertyWithValue("codigo", "MONTO_INVALIDO");
        verifyNoInteractions(cuentas, publicador);
    }

    @Test
    void noMuestraSaldoDeCuentasQueNoOperanEnCajero() {
        when(cuentas.obtener(120L)).thenReturn(new CuentaOrigen(120L, "HIPOTECA", new BigDecimal("8000")));

        assertThatThrownBy(() -> servicio.saldo(120L))
                .hasFieldOrPropertyWithValue("codigo", "CUENTA_NO_HABILITADA");
        verify(cuentas, never()).debitar(any(), any(), anyString());
    }
}
