package cl.duoc.bancoxyz.bff.cajero.cliente;

import java.math.BigDecimal;

public record CuentaOrigen(Long id, String tipo, BigDecimal saldo) {

    public boolean habilitadaEnCajero() {
        return "AHORRO".equals(tipo);
    }
}
