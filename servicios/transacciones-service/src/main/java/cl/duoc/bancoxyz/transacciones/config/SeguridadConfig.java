package cl.duoc.bancoxyz.transacciones.config;

import cl.duoc.bancoxyz.commons.seguridad.RespuestasSeguridad;
import cl.duoc.bancoxyz.commons.seguridad.SeguridadRecursos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
                        .requestMatchers(HttpMethod.GET, "/api/transacciones/**").hasAuthority("SCOPE_transacciones.read")
                        .anyRequest().denyAll())
                .build();
    }
}
