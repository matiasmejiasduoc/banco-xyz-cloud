package cl.duoc.bancoxyz.movimientos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.duoc.bancoxyz.commons.error.ConflictoException;
import cl.duoc.bancoxyz.movimientos.api.MovimientoRespuesta;
import cl.duoc.bancoxyz.movimientos.api.MovimientoService;
import cl.duoc.bancoxyz.movimientos.api.NuevoMovimiento;
import cl.duoc.bancoxyz.movimientos.api.ResumenMovimientos;
import cl.duoc.bancoxyz.movimientos.dominio.FiltroMovimientos;
import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:movimientos-test;DB_CLOSE_DELAY=-1",
        "spring.kafka.listener.auto-startup=false", "spring.kafka.admin.auto-create=false"})
class MovimientoServiceTest {

    @Autowired
    private MovimientoService servicio;

    @Test
    void resumenCuadraConLosTotalesPorTipo() {
        ResumenMovimientos resumen = servicio.resumir(101L, null, null);

        BigDecimal sumaPorTipo = resumen.porTipo().stream()
                .map(total -> total.tipo().esIngreso() ? total.total() : total.total().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(resumen.cantidadMovimientos()).isPositive();
        assertThat(resumen.flujoNeto()).isEqualByComparingTo(sumaPorTipo);
    }

    @Test
    void listaPrimeroLosMovimientosMasRecientes() {
        var pagina = servicio.listar(new FiltroMovimientos(101L, null, null, null), 0, 5);

        assertThat(pagina.contenido()).hasSize(5);
        assertThat(pagina.contenido()).extracting(MovimientoRespuesta::fecha)
                .isSortedAccordingTo((a, b) -> b.compareTo(a));
    }

    @Test
    void registrarConLaMismaReferenciaDevuelveElMovimientoOriginal() {
        NuevoMovimiento nuevo = new NuevoMovimiento(101L, TipoMovimiento.RETIRO, new BigDecimal("1000"),
                "Retiro de prueba", "PRUEBA", "t-mov-1");

        MovimientoRespuesta primero = servicio.registrar(nuevo);
        MovimientoRespuesta segundo = servicio.registrar(nuevo);

        assertThat(segundo.id()).isEqualTo(primero.id());
    }

    @Test
    void registrosSimultaneosConLaMismaReferenciaCreanUnSoloMovimiento() throws Exception {
        NuevoMovimiento nuevo = new NuevoMovimiento(103L, TipoMovimiento.RETIRO, new BigDecimal("1000"),
                "Retiro de prueba", "PRUEBA", "t-mov-concurrente");
        List<Callable<MovimientoRespuesta>> tareas = Collections.nCopies(10, () -> servicio.registrar(nuevo));

        List<Future<MovimientoRespuesta>> resultados;
        try (ExecutorService ejecutor = Executors.newFixedThreadPool(10)) {
            resultados = ejecutor.invokeAll(tareas);
        }

        Set<Long> ids = new HashSet<>();
        for (Future<MovimientoRespuesta> resultado : resultados) {
            ids.add(resultado.get().id());
        }
        assertThat(ids).hasSize(1);
    }

    @Test
    void rechazaReferenciaReutilizadaEnOtroMovimiento() {
        servicio.registrar(new NuevoMovimiento(102L, TipoMovimiento.RETIRO, new BigDecimal("1000"),
                "Retiro de prueba", "PRUEBA", "t-mov-2"));

        assertThatThrownBy(() -> servicio.registrar(new NuevoMovimiento(102L, TipoMovimiento.RETIRO,
                new BigDecimal("2000"), "Retiro de prueba", "PRUEBA", "t-mov-2")))
                .isInstanceOf(ConflictoException.class);
    }
}
