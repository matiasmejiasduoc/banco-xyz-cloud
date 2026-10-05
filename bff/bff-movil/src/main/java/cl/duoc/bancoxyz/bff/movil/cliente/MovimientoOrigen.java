package cl.duoc.bancoxyz.bff.movil.cliente;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoOrigen(LocalDate fecha, String tipo, BigDecimal monto, String descripcion) {
}
