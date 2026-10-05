package cl.duoc.bancoxyz.movimientos.api;

import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import java.math.BigDecimal;

public record TotalPorTipo(TipoMovimiento tipo, long cantidad, BigDecimal total) {
}
