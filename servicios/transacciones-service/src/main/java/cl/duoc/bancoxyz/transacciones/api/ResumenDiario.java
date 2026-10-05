package cl.duoc.bancoxyz.transacciones.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenDiario(
        LocalDate fecha,
        long cantidad,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        long noClasificadas) {

    static ResumenDiario vacio(LocalDate fecha) {
        BigDecimal cero = BigDecimal.ZERO.setScale(2);
        return new ResumenDiario(fecha, 0, cero, cero, 0);
    }
}
