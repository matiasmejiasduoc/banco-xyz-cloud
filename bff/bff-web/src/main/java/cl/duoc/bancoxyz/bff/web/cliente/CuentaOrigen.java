package cl.duoc.bancoxyz.bff.web.cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CuentaOrigen(
        Long id,
        String titular,
        Integer edad,
        String tipo,
        BigDecimal saldo,
        BigDecimal tasaInteresMensual,
        BigDecimal interesMensualEstimado,
        String observaciones,
        LocalDateTime actualizadaEn) {
}
