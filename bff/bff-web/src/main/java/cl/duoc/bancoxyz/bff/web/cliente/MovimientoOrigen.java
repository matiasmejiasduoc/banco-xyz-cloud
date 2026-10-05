package cl.duoc.bancoxyz.bff.web.cliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimientoOrigen(
        Long id,
        Long cuentaId,
        LocalDate fecha,
        String tipo,
        BigDecimal monto,
        String descripcion,
        String canal,
        String referencia,
        String observaciones,
        LocalDateTime registradoEn) {
}
