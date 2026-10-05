package cl.duoc.bancoxyz.bff.web.cliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenMovimientosOrigen(
        Long cuentaId,
        LocalDate desde,
        LocalDate hasta,
        long cantidadMovimientos,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal flujoNeto,
        List<PorTipo> porTipo,
        LocalDate primerMovimiento,
        LocalDate ultimoMovimiento) {

    public record PorTipo(String tipo, long cantidad, BigDecimal total) {
    }
}
