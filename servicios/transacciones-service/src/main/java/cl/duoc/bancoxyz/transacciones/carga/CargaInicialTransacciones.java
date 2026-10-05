package cl.duoc.bancoxyz.transacciones.carga;

import cl.duoc.bancoxyz.commons.legacy.DatoLegacyInvalidoException;
import cl.duoc.bancoxyz.commons.legacy.FechaLegacyParser;
import cl.duoc.bancoxyz.commons.legacy.FilaCsv;
import cl.duoc.bancoxyz.commons.legacy.LectorCsvLegacy;
import cl.duoc.bancoxyz.commons.legacy.ValorLegacy;
import cl.duoc.bancoxyz.transacciones.dominio.TipoTransaccion;
import cl.duoc.bancoxyz.transacciones.dominio.Transaccion;
import cl.duoc.bancoxyz.transacciones.dominio.TransaccionRepository;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialTransacciones implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicialTransacciones.class);

    private final TransaccionRepository repositorio;
    private final Resource archivo;

    public CargaInicialTransacciones(TransaccionRepository repositorio,
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

        Map<Long, Transaccion> transacciones = new LinkedHashMap<>();
        int descartadas = 0;
        for (FilaCsv fila : filas) {
            try {
                Transaccion transaccion = convertir(fila);
                if (transacciones.putIfAbsent(transaccion.getId(), transaccion) != null) {
                    descartadas++;
                }
            } catch (DatoLegacyInvalidoException e) {
                descartadas++;
                log.debug("Linea {} descartada: {}", fila.linea(), e.getMessage());
            }
        }
        repositorio.saveAll(transacciones.values());

        long noClasificadas = transacciones.values().stream()
                .filter(t -> t.getTipo() == TipoTransaccion.NO_CLASIFICADA).count();
        log.info("Carga de transacciones: {} filas leidas, {} descartadas, {} cargadas ({} sin clasificar)",
                filas.size(), descartadas, transacciones.size(), noClasificadas);
    }

    private Transaccion convertir(FilaCsv fila) {
        Long id = ValorLegacy.entero(fila.valor("id"), "id");
        LocalDate fecha = FechaLegacyParser.parse(fila.valor("fecha"), "fecha");
        BigDecimal monto = ValorLegacy.decimal(fila.valor("monto"), "monto");
        TipoTransaccion tipo = TipoTransaccion.desde(fila.valor("tipo"));

        List<String> observaciones = new ArrayList<>();
        if (tipo == TipoTransaccion.NO_CLASIFICADA) {
            observaciones.add("tipo no reconocido en origen (" + fila.valor("tipo") + ")");
        }
        if (monto.signum() < 0) {
            observaciones.add("monto negativo en origen");
        } else if (monto.signum() == 0) {
            observaciones.add("monto cero");
        }

        return new Transaccion(id, fecha, monto.abs().setScale(2, RoundingMode.HALF_UP), tipo,
                observaciones.isEmpty() ? null : String.join("; ", observaciones));
    }
}
