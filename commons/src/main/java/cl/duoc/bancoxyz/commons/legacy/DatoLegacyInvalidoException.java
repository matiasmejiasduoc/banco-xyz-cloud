package cl.duoc.bancoxyz.commons.legacy;

public class DatoLegacyInvalidoException extends RuntimeException {

    public DatoLegacyInvalidoException(String campo, String motivo) {
        super(campo + " " + motivo);
    }
}
