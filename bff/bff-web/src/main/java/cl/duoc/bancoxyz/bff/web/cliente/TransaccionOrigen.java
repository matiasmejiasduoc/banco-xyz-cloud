package cl.duoc.bancoxyz.bff.web.cliente;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionOrigen(Long id, LocalDate fecha, BigDecimal monto, String tipo, String observaciones) {
}
