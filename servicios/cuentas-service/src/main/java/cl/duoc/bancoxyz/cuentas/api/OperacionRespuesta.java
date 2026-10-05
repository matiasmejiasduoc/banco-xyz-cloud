package cl.duoc.bancoxyz.cuentas.api;

import cl.duoc.bancoxyz.cuentas.dominio.OperacionCuenta;
import cl.duoc.bancoxyz.cuentas.dominio.TipoOperacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OperacionRespuesta(
        String referencia,
        Long cuentaId,
        TipoOperacion tipo,
        BigDecimal monto,
        BigDecimal saldoAnterior,
        BigDecimal saldoActual,
        String canal,
        LocalDateTime fecha) {

    public static OperacionRespuesta de(OperacionCuenta operacion) {
        return new OperacionRespuesta(operacion.getReferencia(), operacion.getCuentaId(), operacion.getTipo(),
                operacion.getMonto(), operacion.getSaldoAnterior(), operacion.getSaldoPosterior(),
                operacion.getCanal(), operacion.getFecha());
    }
}
