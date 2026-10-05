package cl.duoc.bancoxyz.bff.movil.api;

import java.math.BigDecimal;
import java.util.List;

public record InicioMovil(Long id, String tipo, BigDecimal saldo, boolean referencial, List<MovimientoMovil> ultimos) {
}
