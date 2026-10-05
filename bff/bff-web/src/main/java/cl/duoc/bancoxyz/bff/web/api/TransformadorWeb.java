package cl.duoc.bancoxyz.bff.web.api;

import cl.duoc.bancoxyz.bff.web.cliente.CuentaOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.MovimientoOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.ResumenDiarioOrigen;
import cl.duoc.bancoxyz.bff.web.cliente.ResumenMovimientosOrigen;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransformadorWeb {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public CuentaWeb cuenta(CuentaOrigen origen) {
        return new CuentaWeb(origen.id(), origen.titular(), origen.edad(), origen.tipo(),
                descripcionTipo(origen.tipo()), origen.saldo(), origen.tasaInteresMensual(), origen.interesMensualEstimado(),
                origen.saldo().add(origen.interesMensualEstimado()), origen.observaciones(), origen.actualizadaEn());
    }

    public MovimientoWeb movimiento(MovimientoOrigen origen) {
        boolean ingreso = "DEPOSITO".equals(origen.tipo());
        return new MovimientoWeb(origen.id(), origen.fecha(), origen.tipo(), ingreso ? "INGRESO" : "EGRESO",
                origen.monto(), ingreso ? origen.monto() : origen.monto().negate(), origen.descripcion(),
                origen.canal(), origen.referencia(), origen.observaciones());
    }

    public ResumenWeb resumen(ResumenMovimientosOrigen origen) {
        BigDecimal montoTotal = origen.totalIngresos().add(origen.totalEgresos());
        List<ResumenWeb.Distribucion> distribucion = origen.porTipo().stream()
                .map(tipo -> new ResumenWeb.Distribucion(tipo.tipo(), tipo.cantidad(), tipo.total(),
                        porcentaje(tipo.total(), montoTotal)))
                .toList();
        return new ResumenWeb(origen.desde(), origen.hasta(), origen.cantidadMovimientos(), origen.totalIngresos(),
                origen.totalEgresos(), origen.flujoNeto(), distribucion, origen.primerMovimiento(),
                origen.ultimoMovimiento());
    }

    public IndicadoresTransacciones indicadores(List<ResumenDiarioOrigen> serie) {
        long cantidad = serie.stream().mapToLong(ResumenDiarioOrigen::cantidad).sum();
        long noClasificadas = serie.stream().mapToLong(ResumenDiarioOrigen::noClasificadas).sum();
        BigDecimal creditos = serie.stream().map(ResumenDiarioOrigen::totalCreditos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debitos = serie.stream().map(ResumenDiarioOrigen::totalDebitos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new IndicadoresTransacciones(cantidad, creditos, debitos, creditos.subtract(debitos), noClasificadas,
                serie);
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    private String descripcionTipo(String tipo) {
        return switch (tipo) {
            case "AHORRO" -> "Cuenta de ahorro";
            case "PRESTAMO" -> "Credito de consumo";
            case "HIPOTECA" -> "Credito hipotecario";
            default -> tipo;
        };
    }
}
