package cl.duoc.bancoxyz.bff.cajero.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaldoCajero(String cuenta, BigDecimal saldoDisponible, LocalDateTime fecha) {
}
