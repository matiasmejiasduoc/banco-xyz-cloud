package cl.duoc.bancoxyz.commons.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class LectorCsvLegacyTest {

    @Test
    void mapeaColumnasYConservaCamposVacios() {
        String csv = "cuenta_id,fecha,transaccion,monto,descripcion\n110,24-07-2024,retiro,1500,\n\n";

        List<FilaCsv> filas = LectorCsvLegacy.leer(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertThat(filas).hasSize(1);
        assertThat(filas.getFirst().linea()).isEqualTo(2);
        assertThat(filas.getFirst().valor("monto")).isEqualTo("1500");
        assertThat(filas.getFirst().valor("descripcion")).isEmpty();
    }
}
