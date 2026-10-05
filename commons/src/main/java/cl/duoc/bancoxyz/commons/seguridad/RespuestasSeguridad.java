package cl.duoc.bancoxyz.commons.seguridad;

import cl.duoc.bancoxyz.commons.api.ErrorApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RespuestasSeguridad.class);

    private final BearerTokenAuthenticationEntryPoint sinToken = new BearerTokenAuthenticationEntryPoint();
    private final BearerTokenAccessDeniedHandler sinPermiso = new BearerTokenAccessDeniedHandler();
    private final JsonMapper json;

    public RespuestasSeguridad(JsonMapper json) {
        this.json = json;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException excepcion) throws IOException {
        log.warn("Solicitud sin token valido hacia {}: {}", request.getRequestURI(), excepcion.getMessage());
        sinToken.commence(request, response, excepcion);
        escribir(response, HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                "Se requiere un token de acceso valido", request);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException excepcion) throws IOException {
        log.warn("Acceso denegado a {} para {}", request.getRequestURI(),
                request.getUserPrincipal() == null ? "anonimo" : request.getUserPrincipal().getName());
        sinPermiso.handle(request, response, excepcion);
        escribir(response, HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "El token no tiene los permisos necesarios para este recurso", request);
    }

    private void escribir(HttpServletResponse response, HttpStatus estado, String codigo, String mensaje,
            HttpServletRequest request) throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getOutputStream(),
                ErrorApi.de(estado.value(), codigo, mensaje, request.getRequestURI()));
    }
}
