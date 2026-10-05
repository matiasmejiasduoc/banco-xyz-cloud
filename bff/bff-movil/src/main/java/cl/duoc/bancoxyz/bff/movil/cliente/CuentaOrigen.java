package cl.duoc.bancoxyz.bff.movil.cliente;

import java.math.BigDecimal;

public record CuentaOrigen(Long id, String tipo, BigDecimal saldo, Boolean referencial) {

    public CuentaOrigen comoReferencial() {
        return new CuentaOrigen(id, tipo, saldo, Boolean.TRUE);
    }

    public boolean esReferencial() {
        return Boolean.TRUE.equals(referencial);
    }
}
