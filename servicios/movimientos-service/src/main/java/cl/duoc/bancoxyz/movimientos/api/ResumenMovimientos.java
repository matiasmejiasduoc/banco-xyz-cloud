package cl.duoc.bancoxyz.movimientos.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenMovimientos(
        Long cuentaId,
        LocalDate desde,
        LocalDate hasta,
        long cantidadMovimientos,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal flujoNeto,
        List<TotalPorTipo> porTipo,
        LocalDate primerMovimiento,
        LocalDate ultimoMovimiento) {
}
