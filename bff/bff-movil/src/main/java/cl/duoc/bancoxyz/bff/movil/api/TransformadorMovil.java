package cl.duoc.bancoxyz.bff.movil.api;

import cl.duoc.bancoxyz.bff.movil.cliente.CuentaOrigen;
import cl.duoc.bancoxyz.bff.movil.cliente.MovimientoOrigen;
import cl.duoc.bancoxyz.bff.movil.config.MovilProperties;
import cl.duoc.bancoxyz.commons.api.Pagina;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransformadorMovil {

    private static final String SIN_DESCRIPCION = "Sin descripcion";

    private final int largoDetalle;

    public TransformadorMovil(MovilProperties propiedades) {
        this.largoDetalle = propiedades.largoDetalle();
    }

    public InicioMovil inicio(CuentaOrigen cuenta, List<MovimientoOrigen> ultimos) {
        return new InicioMovil(cuenta.id(), cuenta.tipo(), compacto(cuenta.saldo()), cuenta.esReferencial(),
                ultimos.stream().map(this::movimiento).toList());
    }

    public SaldoMovil saldo(CuentaOrigen cuenta) {
        return new SaldoMovil(cuenta.id(), compacto(cuenta.saldo()), cuenta.esReferencial());
    }

    public MovimientosMovil movimientos(Pagina<MovimientoOrigen> pagina) {
        return new MovimientosMovil(pagina.contenido().stream().map(this::movimiento).toList(),
                pagina.haySiguiente() ? pagina.pagina() + 1 : null);
    }

    public MovimientoMovil movimiento(MovimientoOrigen origen) {
        BigDecimal monto = "DEPOSITO".equals(origen.tipo()) ? origen.monto() : origen.monto().negate();
        return new MovimientoMovil(origen.fecha(), detalle(origen), compacto(monto));
    }

    private BigDecimal compacto(BigDecimal monto) {
        BigDecimal sinCeros = monto.stripTrailingZeros();
        return sinCeros.scale() < 0 ? sinCeros.setScale(0) : sinCeros;
    }

    private String detalle(MovimientoOrigen origen) {
        String texto = origen.descripcion() == null || SIN_DESCRIPCION.equals(origen.descripcion())
                ? capitalizar(origen.tipo())
                : origen.descripcion();
        return texto.length() <= largoDetalle ? texto : texto.substring(0, largoDetalle);
    }

    private String capitalizar(String tipo) {
        return tipo.charAt(0) + tipo.substring(1).toLowerCase();
    }
}
