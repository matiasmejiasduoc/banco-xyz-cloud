package cl.duoc.bancoxyz.commons.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FechaLegacyParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"2024-03-18", "2024/03/18", "18-03-2024", "18/03/2024", " 2024-03-18 "})
    void reconoceLosFormatosDelSistemaLegacy(String valor) {
        assertThat(FechaLegacyParser.parse(valor, "fecha")).isEqualTo(LocalDate.of(2024, 3, 18));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "2024-02-30", "18.03.2024", "fecha"})
    void rechazaFechasInvalidas(String valor) {
        assertThatThrownBy(() -> FechaLegacyParser.parse(valor, "fecha"))
                .isInstanceOf(DatoLegacyInvalidoException.class);
    }
}
