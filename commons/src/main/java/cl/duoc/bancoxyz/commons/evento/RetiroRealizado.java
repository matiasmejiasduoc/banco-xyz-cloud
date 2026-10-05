package cl.duoc.bancoxyz.commons.evento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RetiroRealizado(
        String referencia,
        Long cuentaId,
        BigDecimal monto,
        String terminal,
        String canal,
        LocalDateTime fecha) {

    public static final String TOPICO = "banco.retiros";
}
