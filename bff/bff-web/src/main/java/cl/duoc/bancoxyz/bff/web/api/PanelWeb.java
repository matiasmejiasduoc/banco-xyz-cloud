package cl.duoc.bancoxyz.bff.web.api;

import cl.duoc.bancoxyz.bff.web.cliente.EstadisticasCuentasOrigen;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PanelWeb(
        LocalDate desde,
        LocalDate hasta,
        EstadisticasCuentasOrigen cuentas,
        IndicadoresTransacciones transacciones,
        ResumenWeb movimientos,
        List<String> avisos,
        LocalDateTime generadoEn) {
}
