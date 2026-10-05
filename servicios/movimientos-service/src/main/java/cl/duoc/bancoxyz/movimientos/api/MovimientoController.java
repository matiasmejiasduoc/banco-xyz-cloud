package cl.duoc.bancoxyz.movimientos.api;

import cl.duoc.bancoxyz.commons.api.Pagina;
import cl.duoc.bancoxyz.movimientos.dominio.FiltroMovimientos;
import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService servicio;

    public MovimientoController(MovimientoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public Pagina<MovimientoRespuesta> listar(
            @RequestParam(required = false) Long cuentaId,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return servicio.listar(new FiltroMovimientos(cuentaId, tipo, desde, hasta), page, size);
    }

    @GetMapping("/resumen")
    public ResumenMovimientos resumen(
            @RequestParam(required = false) Long cuentaId,
            @RequestParam(required = false) @Min(1900) @Max(2100) Integer anio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (anio != null) {
            return servicio.resumir(cuentaId, LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31));
        }
        return servicio.resumir(cuentaId, desde, hasta);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoRespuesta registrar(@Valid @RequestBody NuevoMovimiento nuevo) {
        return servicio.registrar(nuevo);
    }
}
