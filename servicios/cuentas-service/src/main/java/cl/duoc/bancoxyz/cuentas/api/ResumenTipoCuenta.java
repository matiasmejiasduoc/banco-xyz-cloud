package cl.duoc.bancoxyz.cuentas.api;

import cl.duoc.bancoxyz.cuentas.dominio.TipoCuenta;
import java.math.BigDecimal;

public record ResumenTipoCuenta(TipoCuenta tipo, Long cantidad, BigDecimal saldoTotal) {
}
