package cl.duoc.bancoxyz.bff.web.api;

import java.time.LocalDateTime;
import java.util.List;

public record DetalleCuentaWeb(
        CuentaWeb cuenta,
        ResumenWeb resumen,
        List<MovimientoWeb> ultimosMovimientos,
        LocalDateTime generadoEn) {
}
