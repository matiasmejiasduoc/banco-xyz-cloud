package cl.duoc.bancoxyz.movimientos.api;

import cl.duoc.bancoxyz.movimientos.dominio.Movimiento;
import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimientoRespuesta(
        Long id,
        Long cuentaId,
        LocalDate fecha,
        TipoMovimiento tipo,
        BigDecimal monto,
        String descripcion,
        String canal,
        String referencia,
        String observaciones,
        LocalDateTime registradoEn) {

    public static MovimientoRespuesta de(Movimiento movimiento) {
        return new MovimientoRespuesta(movimiento.getId(), movimiento.getCuentaId(), movimiento.getFecha(),
                movimiento.getTipo(), movimiento.getMonto(), movimiento.getDescripcion(), movimiento.getCanal(),
                movimiento.getReferencia(), movimiento.getObservaciones(), movimiento.getRegistradoEn());
    }
}
