package cl.duoc.bancoxyz.bff.movil.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("servicios")
public record ServiciosProperties(Destino cuentas, Destino movimientos) {

    public record Destino(URI url, Duration timeoutConexion, Duration timeoutLectura) {
    }
}
