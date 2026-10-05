package cl.duoc.bancoxyz.bff.cajero.api;

import org.springframework.http.HttpStatus;

public class OperacionCajeroException extends RuntimeException {

    private final HttpStatus estado;
    private final String codigo;

    public OperacionCajeroException(HttpStatus estado, String codigo, String mensaje) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }
}
