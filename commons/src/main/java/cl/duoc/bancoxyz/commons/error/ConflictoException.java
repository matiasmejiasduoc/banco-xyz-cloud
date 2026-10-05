package cl.duoc.bancoxyz.commons.error;

public class ConflictoException extends RuntimeException {

    private final String codigo;

    public ConflictoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
