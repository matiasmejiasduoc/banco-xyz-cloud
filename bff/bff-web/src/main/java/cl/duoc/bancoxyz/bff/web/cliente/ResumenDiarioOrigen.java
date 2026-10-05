package cl.duoc.bancoxyz.bff.web.cliente;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenDiarioOrigen(
        LocalDate fecha,
        long cantidad,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        long noClasificadas) {
}
