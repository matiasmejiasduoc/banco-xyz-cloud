package cl.duoc.bancoxyz.bff.cajero.error;

import cl.duoc.bancoxyz.bff.cajero.api.OperacionCajeroException;
import cl.duoc.bancoxyz.commons.api.ErrorApi;
import cl.duoc.bancoxyz.commons.error.ManejadorErroresApi;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class ManejadorErroresCajero extends ManejadorErroresApi {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErroresCajero.class);

    @ExceptionHandler(OperacionCajeroException.class)
    public ResponseEntity<ErrorApi> operacion(OperacionCajeroException e, HttpServletRequest request) {
        return responder(e.getEstado(), e.getCodigo(), e.getMessage(), request);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorApi> respuestaDeServicio(RestClientResponseException e, HttpServletRequest request) {
        ErrorApi origen = leerError(e);
        String codigo = origen == null ? "" : origen.codigo();
        return switch (e.getStatusCode().value()) {
            case 404 -> responder(HttpStatus.NOT_FOUND, "CUENTA_NO_ENCONTRADA", "Cuenta no encontrada", request);
            case 409 -> "OPERACION_REVERTIDA".equals(codigo)
                    ? responder(HttpStatus.CONFLICT, codigo,
                            "La operacion fue revertida, inicie un nuevo retiro", request)
                    : responder(HttpStatus.CONFLICT, "OPERACION_DUPLICADA",
                            "La clave de la operacion ya fue usada con otros datos", request);
            case 422 -> switch (codigo) {
                case "SALDO_INSUFICIENTE" -> responder(HttpStatus.UNPROCESSABLE_CONTENT, codigo,
                        "Saldo insuficiente", request);
                case "RETIRO_NO_PERMITIDO" -> responder(HttpStatus.UNPROCESSABLE_CONTENT, "CUENTA_NO_HABILITADA",
                        "La cuenta no esta habilitada para operar en cajero", request);
                default -> operacionRechazada(request);
            };
            default -> {
                log.error("Servicio backend respondio {} en {}", e.getStatusCode(), request.getRequestURI());
                yield servicioNoDisponible(request);
            }
        };
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorApi> circuitoAbierto(CallNotPermittedException e, HttpServletRequest request) {
        log.warn("Circuito abierto en {}: {}", request.getRequestURI(), e.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "CIRCUITO_ABIERTO",
                "El servicio esta temporalmente suspendido, intente nuevamente en unos segundos", request);
    }

    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ErrorApi> saturado(BulkheadFullException e, HttpServletRequest request) {
        log.warn("Limite de llamadas concurrentes alcanzado en {}", request.getRequestURI());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_SATURADO",
                "Hay demasiadas solicitudes en curso, intente nuevamente", request);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorApi> sinConexion(ResourceAccessException e, HttpServletRequest request) {
        log.error("Servicio backend no disponible en {}: {}", request.getRequestURI(), e.getMessage());
        return servicioNoDisponible(request);
    }

    private ResponseEntity<ErrorApi> operacionRechazada(HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, "OPERACION_RECHAZADA", "Operacion rechazada", request);
    }

    private ResponseEntity<ErrorApi> servicioNoDisponible(HttpServletRequest request) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE",
                "Servicio temporalmente no disponible", request);
    }

    private ErrorApi leerError(RestClientResponseException e) {
        try {
            return e.getResponseBodyAs(ErrorApi.class);
        } catch (RuntimeException ignorada) {
            return null;
        }
    }
}
