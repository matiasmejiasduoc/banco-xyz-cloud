package cl.duoc.bancoxyz.bff.cajero.cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OperacionOrigen(String referencia, Long cuentaId, BigDecimal monto, BigDecimal saldoActual,
        LocalDateTime fecha) {
}
