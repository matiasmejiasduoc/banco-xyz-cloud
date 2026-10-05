package cl.duoc.bancoxyz.bff.web.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoWeb(
        Long id,
        LocalDate fecha,
        String tipo,
        String naturaleza,
        BigDecimal monto,
        BigDecimal montoConSigno,
        String descripcion,
        String canal,
        String referencia,
        String observaciones) {
}
