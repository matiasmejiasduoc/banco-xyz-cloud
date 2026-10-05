package cl.duoc.bancoxyz.commons.legacy;

import java.util.Map;

public record FilaCsv(int linea, String original, Map<String, String> valores) {

    public String valor(String columna) {
        return valores.get(columna);
    }
}
