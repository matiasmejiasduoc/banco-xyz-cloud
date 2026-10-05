package cl.duoc.bancoxyz.cuentas.dominio;

import cl.duoc.bancoxyz.commons.legacy.ValorLegacy;
import java.util.Optional;

public enum TipoCuenta {
    AHORRO,
    PRESTAMO,
    HIPOTECA;

    public static Optional<TipoCuenta> desde(String valor) {
        return switch (ValorLegacy.normalizado(valor)) {
            case "ahorro" -> Optional.of(AHORRO);
            case "prestamo" -> Optional.of(PRESTAMO);
            case "hipoteca" -> Optional.of(HIPOTECA);
            default -> Optional.empty();
        };
    }

    public boolean permiteRetiros() {
        return this == AHORRO;
    }
}
