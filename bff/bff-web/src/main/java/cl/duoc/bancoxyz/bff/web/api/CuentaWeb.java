package cl.duoc.bancoxyz.bff.web.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CuentaWeb(
        Long id,
        String titular,
        Integer edad,
        String tipo,
        String tipoDescripcion,
        BigDecimal saldo,
        BigDecimal tasaInteresMensual,
        BigDecimal interesMensualEstimado,
        BigDecimal saldoProyectado,
        String observaciones,
        LocalDateTime actualizadaEn) {
}
