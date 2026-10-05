package cl.duoc.bancoxyz.cuentas.api;

import cl.duoc.bancoxyz.cuentas.dominio.TipoCuenta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CuentaRespuesta(
        Long id,
        String titular,
        Integer edad,
        TipoCuenta tipo,
        BigDecimal saldo,
        BigDecimal tasaInteresMensual,
        BigDecimal interesMensualEstimado,
        String observaciones,
        LocalDateTime actualizadaEn) {
}
