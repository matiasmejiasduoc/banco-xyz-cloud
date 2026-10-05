package cl.duoc.bancoxyz.bff.web.config;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientesConfig {

    @Bean
    RestClient cuentasRestClient(RestClient.Builder builder, ServiciosProperties servicios,
            OAuth2ClientHttpRequestInterceptor tokenServicios) {
        return crear(builder, servicios.cuentas(), tokenServicios);
    }

    @Bean
    RestClient movimientosRestClient(RestClient.Builder builder, ServiciosProperties servicios,
            OAuth2ClientHttpRequestInterceptor tokenServicios) {
        return crear(builder, servicios.movimientos(), tokenServicios);
    }

    @Bean
    RestClient transaccionesRestClient(RestClient.Builder builder, ServiciosProperties servicios,
            OAuth2ClientHttpRequestInterceptor tokenServicios) {
        return crear(builder, servicios.transacciones(), tokenServicios);
    }

    @Bean(destroyMethod = "close")
    ExecutorService ejecutorAgregacion() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    private RestClient crear(RestClient.Builder builder, ServiciosProperties.Destino destino,
            OAuth2ClientHttpRequestInterceptor tokenServicios) {
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
}
