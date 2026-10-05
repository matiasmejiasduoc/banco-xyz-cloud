package cl.duoc.bancoxyz.commons.legacy;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

public final class ValorLegacy {

    private ValorLegacy() {
    }

    public static Optional<String> texto(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(valor.trim());
    }

    public static String normalizado(String valor) {
        return texto(valor)
                .map(v -> Normalizer.normalize(v.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", ""))
                .orElse("");
    }

    public static Long entero(String valor, String campo) {
        String limpio = texto(valor).orElseThrow(() -> new DatoLegacyInvalidoException(campo, "vacio"));
        try {
            return Long.valueOf(limpio);
        } catch (NumberFormatException e) {
            throw new DatoLegacyInvalidoException(campo, "no numerico: " + limpio);
        }
    }

    public static BigDecimal decimal(String valor, String campo) {
        String limpio = texto(valor).orElseThrow(() -> new DatoLegacyInvalidoException(campo, "vacio"));
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            throw new DatoLegacyInvalidoException(campo, "no numerico: " + limpio);
        }
    }
}
