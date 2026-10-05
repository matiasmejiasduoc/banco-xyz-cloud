package cl.duoc.bancoxyz.bff.web.cliente;

import java.math.BigDecimal;
import java.util.List;

public record EstadisticasCuentasOrigen(long totalCuentas, BigDecimal saldoTotal, List<PorTipo> porTipo) {

    public record PorTipo(String tipo, long cantidad, BigDecimal saldoTotal) {
    }
}
