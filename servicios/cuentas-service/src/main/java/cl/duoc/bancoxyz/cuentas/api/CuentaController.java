package cl.duoc.bancoxyz.cuentas.api;

import cl.duoc.bancoxyz.commons.api.Pagina;
import cl.duoc.bancoxyz.cuentas.dominio.TipoCuenta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService servicio;

    public CuentaController(CuentaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public Pagina<CuentaRespuesta> listar(
            @RequestParam(required = false) TipoCuenta tipo,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return servicio.listar(tipo, page, size);
    }

    @GetMapping("/estadisticas")
    public EstadisticasCuentas estadisticas() {
        return servicio.estadisticas();
    }

    @GetMapping("/{id}")
    public CuentaRespuesta obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping("/{id}/debitos")
    public OperacionRespuesta debitar(@PathVariable Long id, @Valid @RequestBody OperacionSolicitud solicitud) {
        return servicio.debitar(id, solicitud);
    }

    @PostMapping("/{id}/debitos/{referencia}/reversa")
    public OperacionRespuesta revertirDebito(@PathVariable Long id, @PathVariable String referencia) {
        return servicio.revertirDebito(id, referencia);
    }

    @PostMapping("/{id}/creditos")
    public OperacionRespuesta acreditar(@PathVariable Long id, @Valid @RequestBody OperacionSolicitud solicitud) {
        return servicio.acreditar(id, solicitud);
    }
}
