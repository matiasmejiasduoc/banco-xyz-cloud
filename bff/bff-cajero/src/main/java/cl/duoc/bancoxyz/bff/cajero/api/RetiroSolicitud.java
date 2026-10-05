package cl.duoc.bancoxyz.bff.cajero.api;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RetiroSolicitud(@NotNull BigDecimal monto) {
}
