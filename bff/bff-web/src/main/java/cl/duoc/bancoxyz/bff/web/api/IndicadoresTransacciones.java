package cl.duoc.bancoxyz.bff.web.api;

import cl.duoc.bancoxyz.bff.web.cliente.ResumenDiarioOrigen;
import java.math.BigDecimal;
import java.util.List;

public record IndicadoresTransacciones(
        long cantidad,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        BigDecimal balance,
        long noClasificadas,
        List<ResumenDiarioOrigen> serieDiaria) {
}
