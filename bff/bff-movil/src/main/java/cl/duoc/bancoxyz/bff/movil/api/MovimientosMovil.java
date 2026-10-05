package cl.duoc.bancoxyz.bff.movil.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MovimientosMovil(List<MovimientoMovil> items, Integer siguiente) {
}
