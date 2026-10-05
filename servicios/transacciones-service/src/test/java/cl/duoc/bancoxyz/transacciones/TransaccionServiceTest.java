package cl.duoc.bancoxyz.transacciones;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.bancoxyz.transacciones.api.ResumenDiario;
import cl.duoc.bancoxyz.transacciones.api.TransaccionService;
import cl.duoc.bancoxyz.transacciones.dominio.FiltroTransacciones;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:transacciones-test;DB_CLOSE_DELAY=-1")
class TransaccionServiceTest {

    @Autowired
    private TransaccionService servicio;

    @Test
    void elResumenDiarioCuentaTodasLasTransaccionesDelRango() {
        LocalDate desde = LocalDate.of(2024, 1, 1);
        LocalDate hasta = LocalDate.of(2024, 12, 31);

        List<ResumenDiario> resumen = servicio.resumenDiario(desde, hasta);
        long total = servicio.listar(new FiltroTransacciones(null, desde, hasta), 0, 1).totalElementos();

        assertThat(resumen).isNotEmpty();
        assertThat(resumen).extracting(ResumenDiario::fecha).isSorted();
        assertThat(resumen.stream().mapToLong(ResumenDiario::cantidad).sum()).isEqualTo(total);
    }
}
