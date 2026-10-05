package cl.duoc.bancoxyz.bff.web.api;

import cl.duoc.bancoxyz.bff.web.cliente.CuentaOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.CuentasCliente;
import cl.duoc.bancoxyz.bff.web.cliente.EstadisticasCuentasOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.MovimientoOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.MovimientosCliente;
import cl.duoc.bancoxyz.bff.web.cliente.ResumenDiarioOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.ResumenMovimientosOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.TransaccionOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.TransaccionesCliente;
import cl.duoc.bancoxyz.commons.api.Pagina;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VistaWebService {

    private final CuentasCliente cuentas;
    private final MovimientosCliente movimientos;
    private final TransaccionesCliente transacciones;
    private final TransformadorWeb transformador;
    private final Agregador agregador;
    private final int ultimosMovimientos;

    public VistaWebService(CuentasCliente cuentas, MovimientosCliente movimientos,
            TransaccionesCliente transacciones, TransformadorWeb transformador, Agregador agregador,
            @Value("${web.detalle.ultimos-movimientos}") int ultimosMovimientos) {
        this.cuentas = cuentas;
        this.movimientos = movimientos;
        this.transacciones = transacciones;
        this.transformador = transformador;
        this.agregador = agregador;
        this.ultimosMovimientos = ultimosMovimientos;
    }

    public Pagina<CuentaWeb> listarCuentas(String tipo, int pagina, int tamano) {
        return cuentas.listar(tipo, pagina, tamano).map(transformador::cuenta);
    }

    public DetalleCuentaWeb detalleCuenta(Long id, Integer anio) {
        CompletableFuture<CuentaOrigen> cuenta = agregador.lanzar(() -> cuentas.obtener(id));
        CompletableFuture<ResumenMovimientosOrigen> resumen = agregador.lanzar(
                () -> movimientos.resumen(id, anio, null, null));
        CompletableFuture<Pagina<MovimientoOrigen>> ultimos = agregador.lanzar(
                () -> movimientos.listar(id, null, null, null, 0, ultimosMovimientos));

        return new DetalleCuentaWeb(
                transformador.cuenta(agregador.esperar(cuenta)),
                transformador.resumen(agregador.esperar(resumen)),
                agregador.esperar(ultimos).contenido().stream().map(transformador::movimiento).toList(),
                LocalDateTime.now());
    }

    public Pagina<MovimientoWeb> movimientosCuenta(Long id, String tipo, LocalDate desde, LocalDate hasta,
            int pagina, int tamano) {
        CompletableFuture<CuentaOrigen> cuenta = agregador.lanzar(() -> cuentas.obtener(id));
        CompletableFuture<Pagina<MovimientoOrigen>> consulta = agregador.lanzar(
                () -> movimientos.listar(id, tipo, desde, hasta, pagina, tamano));
        agregador.esperar(cuenta);
        return agregador.esperar(consulta).map(transformador::movimiento);
    }

    public Pagina<TransaccionOrigen> transacciones(String tipo, LocalDate desde, LocalDate hasta, int pagina,
            int tamano) {
        return transacciones.listar(tipo, desde, hasta, pagina, tamano);
    }

    public PanelWeb panel(LocalDate desde, LocalDate hasta) {
        CompletableFuture<EstadisticasCuentasOrigen> estadisticas = agregador.lanzar(cuentas::estadisticas);
        CompletableFuture<List<ResumenDiarioOrigen>> serie = agregador.lanzar(
                () -> transacciones.resumenDiario(desde, hasta));
        CompletableFuture<ResumenMovimientosOrigen> resumen = agregador.lanzar(
                () -> movimientos.resumen(null, null, desde, hasta));

        List<String> avisos = new ArrayList<>();
        EstadisticasCuentasOrigen cuentasPanel = agregador.esperarOpcional(estadisticas, "cuentas", avisos);
        List<ResumenDiarioOrigen> seriePanel = agregador.esperarOpcional(serie, "transacciones", avisos);
        ResumenMovimientosOrigen resumenPanel = agregador.esperarOpcional(resumen, "movimientos", avisos);

        return new PanelWeb(desde, hasta, cuentasPanel,
                seriePanel == null ? null : transformador.indicadores(seriePanel),
                resumenPanel == null ? null : transformador.resumen(resumenPanel),
                avisos, LocalDateTime.now());
    }
}
