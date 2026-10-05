package cl.duoc.bancoxyz.cuentas.api;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OperacionSolicitud(
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal monto,
        @NotBlank @Size(max = 80) String referencia,
        @NotBlank @Size(max = 30) String canal) {
}
