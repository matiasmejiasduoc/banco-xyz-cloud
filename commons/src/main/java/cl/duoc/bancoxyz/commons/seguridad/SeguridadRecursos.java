package cl.duoc.bancoxyz.commons.seguridad;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

public final class SeguridadRecursos {

    private SeguridadRecursos() {
    }

    public static HttpSecurity servidorDeRecursos(HttpSecurity http, RespuestasSeguridad respuestas) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas))
                .oauth2ResourceServer(recursos -> recursos
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas));
    }
}
