package cl.duoc.bancoxyz.commons.error;

import cl.duoc.bancoxyz.commons.api.ErrorApi;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ManejadorErroresApi {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErroresApi.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorApi> noEncontrado(RecursoNoEncontradoException e, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", e.getMessage(), request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorApi> reglaNegocio(ReglaNegocioException e, HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, e.getCodigo(), e.getMessage(), request);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorApi> conflicto(ConflictoException e, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, e.getCodigo(), e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> cuerpoInvalido(MethodArgumentNotValidException e, HttpServletRequest request) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", detalle, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorApi> parametrosInvalidos(HandlerMethodValidationException e,
            HttpServletRequest request) {
        String detalle = e.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(error -> resultado.getMethodParameter().getParameterName() + ": "
                                + error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", detalle, request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorApi> solicitudMalFormada(Exception e, HttpServletRequest request) {
        String detalle = e instanceof MethodArgumentTypeMismatchException tipo
                ? "valor no valido para " + tipo.getName() + ": " + tipo.getValue()
                : e instanceof MissingRequestHeaderException cabecera ? "falta la cabecera " + cabecera.getHeaderName()
                : e instanceof MissingServletRequestParameterException parametro
                        ? "falta el parametro " + parametro.getParameterName()
                : "cuerpo de la solicitud no valido";
        return responder(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", detalle, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> inesperado(Exception e, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrio un error inesperado", request);
    }

    protected ResponseEntity<ErrorApi> responder(HttpStatus estado, String codigo, String mensaje,
            HttpServletRequest request) {
        return ResponseEntity.status(estado)
                .body(ErrorApi.de(estado.value(), codigo, mensaje, request.getRequestURI()));
    }
}
