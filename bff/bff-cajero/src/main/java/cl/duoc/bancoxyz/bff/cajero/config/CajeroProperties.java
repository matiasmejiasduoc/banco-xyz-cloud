package cl.duoc.bancoxyz.bff.cajero.config;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cajero")
public record CajeroProperties(Retiro retiro, Duration esperaPublicacion) {

    public record Retiro(BigDecimal montoMinimo, BigDecimal montoMaximo, BigDecimal multiplo) {
    }
}
