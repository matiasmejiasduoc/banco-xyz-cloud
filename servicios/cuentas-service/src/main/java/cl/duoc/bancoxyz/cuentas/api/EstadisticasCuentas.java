package cl.duoc.bancoxyz.cuentas.api;

import java.math.BigDecimal;
import java.util.List;

public record EstadisticasCuentas(long totalCuentas, BigDecimal saldoTotal, List<ResumenTipoCuenta> porTipo) {
}
