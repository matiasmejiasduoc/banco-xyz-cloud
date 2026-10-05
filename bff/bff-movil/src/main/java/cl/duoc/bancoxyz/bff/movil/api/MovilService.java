package cl.duoc.bancoxyz.bff.movil.api;

import cl.duoc.bancoxyz.bff.movil.cliente.CuentaOrigen;
import cl.duoc.bancoxyz.bff.movil.cliente.CuentasCliente;
import cl.duoc.bancoxyz.bff.movil.cliente.MovimientoOrigen;
import cl.duoc.bancoxyz.bff.movil.cliente.MovimientosCliente;
import cl.duoc.bancoxyz.bff.movil.config.MovilProperties;
import cl.duoc.bancoxyz.commons.api.Pagina;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class MovilService {

    private final CuentasCliente cuentas;
    private final MovimientosCliente movimientos;
    private final TransformadorMovil transformador;
    private final MovilProperties propiedades;
    private final ExecutorService ejecutor;

    public MovilService(CuentasCliente cuentas, MovimientosCliente movimientos, TransformadorMovil transformador,
            MovilProperties propiedades, ExecutorService ejecutorAgregacion) {
        this.cuentas = cuentas;
        this.movimientos = movimientos;
        this.transformador = transformador;
        this.propiedades = propiedades;
        this.ejecutor = ejecutorAgregacion;
    }

    public InicioMovil inicio(Long id) {
        CompletableFuture<CuentaOrigen> cuenta = lanzar(() -> cuentas.obtener(id));
        CompletableFuture<Pagina<MovimientoOrigen>> ultimos = lanzar(
                () -> movimientos.recientes(id, 0, propiedades.ultimosMovimientos()));
        return transformador.inicio(esperar(cuenta), esperar(ultimos).contenido());
    }

    public SaldoMovil saldo(Long id) {
        return transformador.saldo(cuentas.obtener(id));
    }

    public MovimientosMovil movimientos(Long id, int pagina) {
        CompletableFuture<CuentaOrigen> cuenta = lanzar(() -> cuentas.obtener(id));
        CompletableFuture<Pagina<MovimientoOrigen>> consulta = lanzar(
                () -> movimientos.recientes(id, pagina, propiedades.tamanoPagina()));
        esperar(cuenta);
        return transformador.movimientos(esperar(consulta));
    }

    private <T> CompletableFuture<T> lanzar(Supplier<T> consulta) {
        return CompletableFuture.supplyAsync(consulta, ejecutor);
    }

    private <T> T esperar(CompletableFuture<T> futuro) {
        try {
            return futuro.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException causa) {
                throw causa;
            }
            throw e;
        }
    }
}
