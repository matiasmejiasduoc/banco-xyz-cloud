package cl.duoc.bancoxyz.bff.movil.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("movil")
public record MovilProperties(int ultimosMovimientos, int tamanoPagina, int largoDetalle) {
}
