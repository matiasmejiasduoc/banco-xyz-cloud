package cl.duoc.bancoxyz.transacciones.dominio;

import cl.duoc.bancoxyz.commons.legacy.ValorLegacy;

public enum TipoTransaccion {
    CREDITO,
    DEBITO,
    NO_CLASIFICADA;

    public static TipoTransaccion desde(String valor) {
        return switch (ValorLegacy.normalizado(valor)) {
            case "credito" -> CREDITO;
            case "debito" -> DEBITO;
            default -> NO_CLASIFICADA;
        };
    }
}
