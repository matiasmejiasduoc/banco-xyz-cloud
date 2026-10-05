package cl.duoc.bancoxyz.bff.web.cliente;

import cl.duoc.bancoxyz.commons.api.Pagina;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TransaccionesCliente {

    private static final ParameterizedTypeReference<Pagina<TransaccionOrigen>> PAGINA =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<ResumenDiarioOrigen>> RESUMEN =
            new ParameterizedTypeReference<>() {
            };

    private static final String SERVICIO = "transacciones";

    private final RestClient cliente;

    public TransaccionesCliente(@Qualifier("transaccionesRestClient") RestClient cliente) {
        this.cliente = cliente;
    }

    @Retry(name = SERVICIO)
    @CircuitBreaker(name = SERVICIO)
    @Bulkhead(name = SERVICIO)
    public Pagina<TransaccionOrigen> listar(String tipo, LocalDate desde, LocalDate hasta, int pagina, int tamano) {
        return cliente.get()
                .uri(uri -> uri.path("/api/transacciones")
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
    public List<ResumenDiarioOrigen> resumenDiario(LocalDate desde, LocalDate hasta) {
        return cliente.get()
                .uri(uri -> uri.path("/api/transacciones/resumen-diario")
                        .queryParamIfPresent("desde", Optional.ofNullable(desde))
                        .queryParamIfPresent("hasta", Optional.ofNullable(hasta))
                        .build())
                .retrieve()
                .body(RESUMEN);
    }
}
