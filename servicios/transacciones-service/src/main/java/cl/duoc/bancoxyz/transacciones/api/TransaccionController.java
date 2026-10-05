package cl.duoc.bancoxyz.transacciones.api;

import cl.duoc.bancoxyz.commons.api.Pagina;
import cl.duoc.bancoxyz.transacciones.dominio.FiltroTransacciones;
import cl.duoc.bancoxyz.transacciones.dominio.TipoTransaccion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService servicio;

    public TransaccionController(TransaccionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public Pagina<TransaccionRespuesta> listar(
            @RequestParam(required = false) TipoTransaccion tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size) {
        return servicio.listar(new FiltroTransacciones(tipo, desde, hasta), page, size);
    }

    @GetMapping("/resumen-diario")
    public List<ResumenDiario> resumenDiario(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.resumenDiario(desde, hasta);
    }

    @GetMapping("/{id}")
    public TransaccionRespuesta obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }
}
