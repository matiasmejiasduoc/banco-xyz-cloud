package cl.duoc.bancoxyz.bff.movil.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoMovil(LocalDate fecha, String detalle, BigDecimal monto) {
}
