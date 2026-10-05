package cl.duoc.bancoxyz.bff.cajero.cliente;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CuentasCliente {

    private static final String CANAL = "CAJERO";
    private static final String CUENTAS = "cuentas";

    private final RestClient cliente;

    public CuentasCliente(@Qualifier("cuentasRestClient") RestClient cliente) {
        this.cliente = cliente;
    }

    @Retry(name = CUENTAS)
    @CircuitBreaker(name = CUENTAS)
    @Bulkhead(name = CUENTAS)
    public CuentaOrigen obtener(Long id) {
        return cliente.get().uri("/api/cuentas/{id}", id).retrieve().body(CuentaOrigen.class);
    }

    @Retry(name = CUENTAS)
    @CircuitBreaker(name = CUENTAS)
    @Bulkhead(name = CUENTAS)
    public OperacionOrigen debitar(Long id, BigDecimal monto, String referencia) {
        return cliente.post()
                .uri("/api/cuentas/{id}/debitos", id)
                .body(Map.of("monto", monto, "referencia", referencia, "canal", CANAL))
                .retrieve()
                .body(OperacionOrigen.class);
    }

    @Retry(name = CUENTAS)
    public OperacionOrigen revertirDebito(Long id, String referencia) {
        return cliente.post()
                .uri("/api/cuentas/{id}/debitos/{referencia}/reversa", id, referencia)
                .retrieve()
                .body(OperacionOrigen.class);
    }
}
