package cl.duoc.bancoxyz.bff.web.error;

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
public class ManejadorErroresWeb extends ManejadorErroresApi {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErroresWeb.class);

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorApi> respuestaDeServicio(RestClientResponseException e, HttpServletRequest request) {
        HttpStatus estado = HttpStatus.resolve(e.getStatusCode().value());
        if (estado == null || estado.is5xxServerError()) {
            log.error("Servicio backend respondio {} en {}", e.getStatusCode(), request.getRequestURI());
            return responder(HttpStatus.BAD_GATEWAY, "ERROR_SERVICIO_BACKEND",
                    "Uno de los servicios del banco respondio con error", request);
        }
        ErrorApi origen = leerError(e);
        return responder(estado, origen == null ? "ERROR_SERVICIO_BACKEND" : origen.codigo(),
                origen == null ? e.getStatusText() : origen.mensaje(), request);
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
    public ResponseEntity<ErrorApi> servicioNoDisponible(ResourceAccessException e, HttpServletRequest request) {
        log.error("Servicio backend no disponible en {}: {}", request.getRequestURI(), e.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE",
                "Los servicios del banco no estan disponibles en este momento", request);
    }

    private ErrorApi leerError(RestClientResponseException e) {
        try {
            return e.getResponseBodyAs(ErrorApi.class);
        } catch (RuntimeException ignorada) {
            return null;
        }
    }
}
