package cl.duoc.bancoxyz.movimientos.dominio;

import cl.duoc.bancoxyz.commons.legacy.ValorLegacy;
import java.util.Optional;

public enum TipoMovimiento {
    DEPOSITO,
    RETIRO,
    COMPRA,
    PAGO;

    public static Optional<TipoMovimiento> desde(String valor) {
        return switch (ValorLegacy.normalizado(valor)) {
            case "deposito" -> Optional.of(DEPOSITO);
            case "retiro" -> Optional.of(RETIRO);
            case "compra" -> Optional.of(COMPRA);
            case "pago" -> Optional.of(PAGO);
            default -> Optional.empty();
        };
    }

    public boolean esIngreso() {
        return this == DEPOSITO;
    }
}
