package cl.duoc.bancoxyz.movimientos.api;

import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record NuevoMovimiento(
        @NotNull @Positive Long cuentaId,
        @NotNull TipoMovimiento tipo,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal monto,
        @NotBlank @Size(max = 120) String descripcion,
        @NotBlank @Size(max = 30) String canal,
        @Size(max = 80) String referencia) {
}
