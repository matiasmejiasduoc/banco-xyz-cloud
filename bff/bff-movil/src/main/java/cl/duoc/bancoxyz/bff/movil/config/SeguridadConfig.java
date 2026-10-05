package cl.duoc.bancoxyz.bff.movil.config;

import cl.duoc.bancoxyz.commons.seguridad.RespuestasSeguridad;
import cl.duoc.bancoxyz.commons.seguridad.SeguridadRecursos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SeguridadConfig {

    @Bean
    RespuestasSeguridad respuestasSeguridad(JsonMapper json) {
        return new RespuestasSeguridad(json);
    }

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http, RespuestasSeguridad respuestas) throws Exception {
        return SeguridadRecursos.servidorDeRecursos(http, respuestas)
                .authorizeHttpRequests(acceso -> acceso
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/movil/api/**").hasAuthority("SCOPE_movil.read")
                        .anyRequest().denyAll())
                .build();
    }

    @Bean
    OAuth2AuthorizedClientManager gestorTokens(ClientRegistrationRepository registros,
            OAuth2AuthorizedClientService clientesAutorizados) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager gestor =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(registros, clientesAutorizados);
        gestor.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build());
        return gestor;
    }

    @Bean
    OAuth2ClientHttpRequestInterceptor tokenServicios(OAuth2AuthorizedClientManager gestorTokens,
            @Value("${servicios.registro-oauth}") String registro,
            @Value("${spring.application.name}") String aplicacion) {
        Authentication bff = new AnonymousAuthenticationToken(aplicacion, aplicacion,
                AuthorityUtils.createAuthorityList("ROLE_SERVICIO"));
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(gestorTokens);
        interceptor.setClientRegistrationIdResolver(solicitud -> registro);
        interceptor.setPrincipalResolver(solicitud -> bff);
        return interceptor;
    }
}
