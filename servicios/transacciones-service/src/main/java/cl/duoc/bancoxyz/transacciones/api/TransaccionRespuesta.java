package cl.duoc.bancoxyz.transacciones.api;

import cl.duoc.bancoxyz.transacciones.dominio.TipoTransaccion;
import cl.duoc.bancoxyz.transacciones.dominio.Transaccion;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionRespuesta(Long id, LocalDate fecha, BigDecimal monto, TipoTransaccion tipo,
        String observaciones) {

    public static TransaccionRespuesta de(Transaccion transaccion) {
        return new TransaccionRespuesta(transaccion.getId(), transaccion.getFecha(), transaccion.getMonto(),
                transaccion.getTipo(), transaccion.getObservaciones());
    }
}
