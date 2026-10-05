package cl.duoc.bancoxyz.bff.web.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenWeb(
        LocalDate desde,
        LocalDate hasta,
        long cantidadMovimientos,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal flujoNeto,
        List<Distribucion> distribucion,
        LocalDate primerMovimiento,
        LocalDate ultimoMovimiento) {

    public record Distribucion(String tipo, long cantidad, BigDecimal total, BigDecimal porcentajeDelMonto) {
    }
}
