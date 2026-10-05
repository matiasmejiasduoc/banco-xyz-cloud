package cl.duoc.bancoxyz.bff.cajero.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cajero/api/cuentas/{id}")
public class CajeroController {

    private final CajeroService servicio;

    public CajeroController(CajeroService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/saldo")
    public SaldoCajero saldo(@PathVariable Long id) {
        return servicio.saldo(id);
    }

    @PostMapping("/retiros")
    public ComprobanteRetiro retirar(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt terminal,
            @RequestHeader("Idempotency-Key") @Pattern(regexp = "[A-Za-z0-9-]{8,40}") String claveIdempotencia,
            @Valid @RequestBody RetiroSolicitud solicitud) {
        return servicio.retirar(id, solicitud.monto(), terminal.getSubject(), claveIdempotencia);
    }
}
