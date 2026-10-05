package cl.duoc.bancoxyz.bff.movil.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movil/api/cuentas/{id}")
public class MovilController {

    private final MovilService servicio;

    public MovilController(MovilService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/inicio")
    public InicioMovil inicio(@PathVariable Long id) {
        return servicio.inicio(id);
    }

    @GetMapping("/saldo")
    public SaldoMovil saldo(@PathVariable Long id) {
        return servicio.saldo(id);
    }

    @GetMapping("/movimientos")
    public MovimientosMovil movimientos(@PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) @Max(1000) int pagina) {
        return servicio.movimientos(id, pagina);
    }
}
