package cl.duoc.bancoxyz.bff.movil.cliente;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class CuentasCliente {

    private static final Logger log = LoggerFactory.getLogger(CuentasCliente.class);
    private static final String SERVICIO = "cuentas";

    private final RestClient cliente;
    private final Map<Long, CuentaOrigen> ultimasConocidas = new ConcurrentHashMap<>();

    public CuentasCliente(@Qualifier("cuentasRestClient") RestClient cliente) {
        this.cliente = cliente;
    }

    @Retry(name = SERVICIO)
    @CircuitBreaker(name = SERVICIO, fallbackMethod = "ultimaConocida")
    @Bulkhead(name = SERVICIO)
    public CuentaOrigen obtener(Long id) {
        CuentaOrigen cuenta = cliente.get().uri("/api/cuentas/{id}", id).retrieve().body(CuentaOrigen.class);
        if (cuenta != null) {
            ultimasConocidas.put(id, cuenta);
        }
        return cuenta;
    }

    CuentaOrigen ultimaConocida(Long id, CallNotPermittedException e) {
        return desdeRespaldo(id, e);
    }

    CuentaOrigen ultimaConocida(Long id, BulkheadFullException e) {
        return desdeRespaldo(id, e);
    }

    CuentaOrigen ultimaConocida(Long id, ResourceAccessException e) {
        return desdeRespaldo(id, e);
    }

    CuentaOrigen ultimaConocida(Long id, HttpServerErrorException e) {
        return desdeRespaldo(id, e);
    }

    private <E extends RuntimeException> CuentaOrigen desdeRespaldo(Long id, E causa) {
        CuentaOrigen conocida = ultimasConocidas.get(id);
        if (conocida == null) {
            throw causa;
        }
        log.warn("cuentas-service no disponible ({}), se responde con el ultimo saldo conocido de la cuenta {}",
                causa.getClass().getSimpleName(), id);
        return conocida.comoReferencial();
    }
}
