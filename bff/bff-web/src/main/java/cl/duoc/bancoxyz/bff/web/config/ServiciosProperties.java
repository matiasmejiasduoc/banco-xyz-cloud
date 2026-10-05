package cl.duoc.bancoxyz.bff.web.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("servicios")
public record ServiciosProperties(Destino cuentas, Destino movimientos, Destino transacciones) {

    public record Destino(URI url, Duration timeoutConexion, Duration timeoutLectura) {
    }
}
