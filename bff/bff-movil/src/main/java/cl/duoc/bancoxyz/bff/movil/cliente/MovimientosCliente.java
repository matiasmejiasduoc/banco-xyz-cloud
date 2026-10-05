package cl.duoc.bancoxyz.bff.movil.cliente;

import cl.duoc.bancoxyz.commons.api.Pagina;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MovimientosCliente {

    private static final ParameterizedTypeReference<Pagina<MovimientoOrigen>> PAGINA =
            new ParameterizedTypeReference<>() {
            };

    private static final String SERVICIO = "movimientos";

    private final RestClient cliente;

    public MovimientosCliente(@Qualifier("movimientosRestClient") RestClient cliente) {
        this.cliente = cliente;
    }

    @Retry(name = SERVICIO)
    @CircuitBreaker(name = SERVICIO)
    @Bulkhead(name = SERVICIO)
    public Pagina<MovimientoOrigen> recientes(Long cuentaId, int pagina, int tamano) {
        return cliente.get()
                .uri(uri -> uri.path("/api/movimientos")
                        .queryParam("cuentaId", cuentaId)
                        .queryParam("page", pagina)
                        .queryParam("size", tamano)
                        .build())
                .retrieve()
                .body(PAGINA);
    }
}
