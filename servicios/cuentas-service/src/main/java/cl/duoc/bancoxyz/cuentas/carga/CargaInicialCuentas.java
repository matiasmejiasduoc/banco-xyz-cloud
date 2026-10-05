package cl.duoc.bancoxyz.cuentas.carga;

import cl.duoc.bancoxyz.commons.legacy.DatoLegacyInvalidoException;
import cl.duoc.bancoxyz.commons.legacy.FilaCsv;
import cl.duoc.bancoxyz.commons.legacy.LectorCsvLegacy;
import cl.duoc.bancoxyz.commons.legacy.ValorLegacy;
import cl.duoc.bancoxyz.cuentas.dominio.Cuenta;
import cl.duoc.bancoxyz.cuentas.dominio.CuentaRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialCuentas implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicialCuentas.class);

    private final CuentaRepository repositorio;
    private final Resource archivo;

    public CargaInicialCuentas(CuentaRepository repositorio, @Value("${banco.carga.archivo}") Resource archivo) {
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

        Map<Long, ConsolidadorCuenta> consolidadores = new LinkedHashMap<>();
        int sinIdentificador = 0;
        for (FilaCsv fila : filas) {
            try {
                Long id = ValorLegacy.entero(fila.valor("cuenta_id"), "cuenta_id");
                consolidadores.computeIfAbsent(id, ConsolidadorCuenta::new).agregar(fila);
            } catch (DatoLegacyInvalidoException e) {
                sinIdentificador++;
                log.debug("Linea {} descartada: {}", fila.linea(), e.getMessage());
            }
        }

        List<Cuenta> cuentas = consolidadores.values().stream()
                .map(ConsolidadorCuenta::consolidar)
                .flatMap(Optional::stream)
                .toList();
        repositorio.saveAll(cuentas);

        log.info("Carga de cuentas: {} filas leidas, {} sin identificador, {} consolidadas, {} sin datos suficientes",
                filas.size(), sinIdentificador, cuentas.size(), consolidadores.size() - cuentas.size());
    }
}
