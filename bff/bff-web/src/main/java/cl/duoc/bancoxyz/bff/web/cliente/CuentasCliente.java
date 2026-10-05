package cl.duoc.bancoxyz.bff.web.cliente;

import cl.duoc.bancoxyz.commons.api.Pagina;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CuentasCliente {

    private static final ParameterizedTypeReference<Pagina<CuentaOrigen>> PAGINA =
            new ParameterizedTypeReference<>() {
            };

    private static final String SERVICIO = "cuentas";

    private final RestClient cliente;

    public CuentasCliente(@Qualifier("cuentasRestClient") RestClient cliente) {
        this.cliente = cliente;
    }

    @Retry(name = SERVICIO)
    @CircuitBreaker(name = SERVICIO)
    @Bulkhead(name = SERVICIO)
    public Pagina<CuentaOrigen> listar(String tipo, int pagina, int tamano) {
        return cliente.get()
                .uri(uri -> uri.path("/api/cuentas")
                        .queryParamIfPresent("tipo", Optional.ofNullable(tipo))
                        .queryParam("page", pagina)
                        .queryParam("size", tamano)
                        .build())
                .retrieve()
                .body(PAGINA);
    }

    @Retry(name = SERVICIO)
    @CircuitBreaker(name = SERVICIO)
    @Bulkhead(name = SERVICIO)
    public CuentaOrigen obtener(Long id) {
        return cliente.get().uri("/api/cuentas/{id}", id).retrieve().body(CuentaOrigen.class);
    }

    @Retry(name = SERVICIO)
    @CircuitBreaker(name = SERVICIO)
    @Bulkhead(name = SERVICIO)
    public EstadisticasCuentasOrigen estadisticas() {
        return cliente.get().uri("/api/cuentas/estadisticas").retrieve().body(EstadisticasCuentasOrigen.class);
    }
}
