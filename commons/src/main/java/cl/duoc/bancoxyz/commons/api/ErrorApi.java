package cl.duoc.bancoxyz.commons.api;

import java.time.LocalDateTime;

public record ErrorApi(int estado, String codigo, String mensaje, String ruta, LocalDateTime fecha) {

    public static ErrorApi de(int estado, String codigo, String mensaje, String ruta) {
        return new ErrorApi(estado, codigo, mensaje, ruta, LocalDateTime.now());
    }
}
