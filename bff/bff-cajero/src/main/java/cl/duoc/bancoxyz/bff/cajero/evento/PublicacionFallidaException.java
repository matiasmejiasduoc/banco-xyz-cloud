package cl.duoc.bancoxyz.bff.cajero.evento;

public class PublicacionFallidaException extends RuntimeException {

    public PublicacionFallidaException(String referencia, Throwable causa) {
        super("No se pudo publicar el evento del retiro " + referencia, causa);
    }
}
