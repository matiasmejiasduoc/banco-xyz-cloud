package cl.duoc.bancoxyz.bff.cajero.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComprobanteRetiro(String operacion, String cuenta, BigDecimal monto, BigDecimal saldoDisponible,
        LocalDateTime fecha) {
}
