package cl.duoc.bancoxyz.movimientos.carga;

import cl.duoc.bancoxyz.commons.legacy.DatoLegacyInvalidoException;
import cl.duoc.bancoxyz.commons.legacy.FechaLegacyParser;
import cl.duoc.bancoxyz.commons.legacy.FilaCsv;
import cl.duoc.bancoxyz.commons.legacy.LectorCsvLegacy;
import cl.duoc.bancoxyz.commons.legacy.ValorLegacy;
import cl.duoc.bancoxyz.movimientos.dominio.Movimiento;
import cl.duoc.bancoxyz.movimientos.dominio.MovimientoRepository;
import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialMovimientos implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicialMovimientos.class);
    private static final String CANAL_LEGACY = "LEGACY";
    private static final String SIN_DESCRIPCION = "Sin descripcion";

    private final MovimientoRepository repositorio;
    private final Resource archivo;

    public CargaInicialMovimientos(MovimientoRepository repositorio,
            @Value("${banco.carga.archivo}") Resource archivo) {
        this.repositorio = repositorio;
        this.archivo = archivo;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (repositorio.count() > 0) {
            return;
        }
        List<FilaCsv> filas;
        try (InputStream entrada = archivo.getInputStream()) {
            filas = LectorCsvLegacy.leer(entrada);
        }

        Set<String> vistas = new HashSet<>();
        List<Movimiento> movimientos = new ArrayList<>();
        int duplicadas = 0;
        int descartadas = 0;
        for (FilaCsv fila : filas) {
            if (!vistas.add(fila.original())) {
                duplicadas++;
                continue;
            }
            try {
                movimientos.add(convertir(fila));
            } catch (DatoLegacyInvalidoException e) {
                descartadas++;
                log.debug("Linea {} descartada: {}", fila.linea(), e.getMessage());
            }
        }
        repositorio.saveAll(movimientos);

        log.info("Carga de movimientos: {} filas leidas, {} duplicadas, {} descartadas, {} movimientos cargados",
                filas.size(), duplicadas, descartadas, movimientos.size());
    }

    private Movimiento convertir(FilaCsv fila) {
        Long cuentaId = ValorLegacy.entero(fila.valor("cuenta_id"), "cuenta_id");
        LocalDate fecha = FechaLegacyParser.parse(fila.valor("fecha"), "fecha");
        TipoMovimiento tipo = TipoMovimiento.desde(fila.valor("transaccion"))
                .orElseThrow(() -> new DatoLegacyInvalidoException("transaccion", "no reconocida"));
        BigDecimal monto = ValorLegacy.decimal(fila.valor("monto"), "monto");
        if (monto.signum() == 0) {
            throw new DatoLegacyInvalidoException("monto", "en cero");
        }

        List<String> observaciones = new ArrayList<>();
        if (monto.signum() < 0) {
            observaciones.add("monto negativo en origen");
        }
        Optional<String> descripcion = ValorLegacy.texto(fila.valor("descripcion"));
        if (descripcion.isEmpty()) {
            observaciones.add("descripcion ausente en origen");
        }

        return new Movimiento(cuentaId, fecha, tipo, monto.abs().setScale(2, RoundingMode.HALF_UP),
                descripcion.orElse(SIN_DESCRIPCION), CANAL_LEGACY, null,
                observaciones.isEmpty() ? null : String.join("; ", observaciones));
    }
}
