package cl.duoc.bancoxyz.bff.web.cliente;

import cl.duoc.bancoxyz.commons.api.Pagina;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.LocalDate;
import java.util.Optional;
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
    public Pagina<MovimientoOrigen> listar(Long cuentaId, String tipo, LocalDate desde, LocalDate hasta, int pagina,
            int tamano) {
        return cliente.get()
                .uri(uri -> uri.path("/api/movimientos")
                        .queryParam("cuentaId", cuentaId)
                        .queryParamIfPresent("tipo", Optional.ofNullable(tipo))
                        .queryParamIfPresent("desde", Optional.ofNullable(desde))
                        .queryParamIfPresent("hasta", Optional.ofNullable(hasta))
                        .queryParam("page", pagina)
                        .queryParam("size", tamano)
                        .build())
                .retrieve()
                .body(PAGINA);
    }

    @Retry(name = SERVICIO)
    @CircuitBreaker(name = SERVICIO)
    @Bulkhead(name = SERVICIO)
    public ResumenMovimientosOrigen resumen(Long cuentaId, Integer anio, LocalDate desde, LocalDate hasta) {
        return cliente.get()
                .uri(uri -> uri.path("/api/movimientos/resumen")
                        .queryParamIfPresent("cuentaId", Optional.ofNullable(cuentaId))
                        .queryParamIfPresent("anio", Optional.ofNullable(anio))
                        .queryParamIfPresent("desde", Optional.ofNullable(desde))
                        .queryParamIfPresent("hasta", Optional.ofNullable(hasta))
                        .build())
                .retrieve()
                .body(ResumenMovimientosOrigen.class);
    }
}
