package cl.duoc.bancoxyz.bff.web.api;

import cl.duoc.bancoxyz.bff.web.cliente.TransaccionOrigen;
import cl.duoc.bancoxyz.commons.api.Pagina;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/api")
public class WebController {

    private final VistaWebService servicio;

    public WebController(VistaWebService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/cuentas")
    public Pagina<CuentaWeb> cuentas(
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int size) {
        return servicio.listarCuentas(tipo, page, size);
    }

    @GetMapping("/cuentas/{id}")
    public DetalleCuentaWeb detalle(@PathVariable Long id,
            @RequestParam(required = false) @Min(1900) @Max(2100) Integer anio) {
        return servicio.detalleCuenta(id, anio);
    }

    @GetMapping("/cuentas/{id}/movimientos")
    public Pagina<MovimientoWeb> movimientos(
            @PathVariable Long id,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return servicio.movimientosCuenta(id, tipo, desde, hasta, page, size);
    }

    @GetMapping("/transacciones")
    public Pagina<TransaccionOrigen> transacciones(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return servicio.transacciones(tipo, desde, hasta, page, size);
    }

    @GetMapping("/panel")
    public PanelWeb panel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.panel(desde, hasta);
    }
}
