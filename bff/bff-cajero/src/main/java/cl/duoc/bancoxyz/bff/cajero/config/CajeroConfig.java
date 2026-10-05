package cl.duoc.bancoxyz.bff.cajero.config;

import cl.duoc.bancoxyz.commons.evento.RetiroRealizado;
import java.net.http.HttpClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class CajeroConfig {

    @Bean
    RestClient cuentasRestClient(RestClient.Builder builder, ServiciosProperties servicios,
            OAuth2ClientHttpRequestInterceptor tokenServicios) {
        ServiciosProperties.Destino destino = servicios.cuentas();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(destino.timeoutConexion())
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(httpClient);
        fabrica.setReadTimeout(destino.timeoutLectura());
        return builder.clone()
                .baseUrl(destino.url().toString())
                .requestFactory(fabrica)
                .requestInterceptor(tokenServicios)
                .build();
    }

    @Bean
    NewTopic topicoRetiros() {
        return TopicBuilder.name(RetiroRealizado.TOPICO).partitions(3).replicas(1).build();
    }
}
