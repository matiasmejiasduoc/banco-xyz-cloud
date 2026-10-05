package cl.duoc.bancoxyz.bff.movil.api;

import java.math.BigDecimal;

public record SaldoMovil(Long id, BigDecimal saldo, boolean referencial) {
}
