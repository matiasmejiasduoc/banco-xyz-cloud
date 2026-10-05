package cl.duoc.bancoxyz.movimientos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.duoc.bancoxyz.commons.evento.RetiroRealizado;
import cl.duoc.bancoxyz.movimientos.api.MovimientoRespuesta;
import cl.duoc.bancoxyz.movimientos.api.MovimientoService;
import cl.duoc.bancoxyz.movimientos.api.NuevoMovimiento;
import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import cl.duoc.bancoxyz.movimientos.evento.RetirosListener;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

class RetirosListenerTest {

    private final MovimientoService servicio = mock(MovimientoService.class);
    private final JsonMapper json = JsonMapper.builder().build();
    private final RetirosListener listener = new RetirosListener(servicio, json,
            Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void registraElRetiroComoMovimientoConLaReferenciaDelEvento() {
        when(servicio.registrar(any())).thenReturn(mock(MovimientoRespuesta.class));
        RetiroRealizado retiro = new RetiroRealizado("CAJ-001-abc12345", 101L, new BigDecimal("2000"), "CAJ-001",
                "CAJERO", LocalDateTime.now());

        listener.recibir(new ConsumerRecord<>(RetiroRealizado.TOPICO, 0, 7L, "101", json.writeValueAsString(retiro)));

        ArgumentCaptor<NuevoMovimiento> movimiento = ArgumentCaptor.forClass(NuevoMovimiento.class);
        verify(servicio).registrar(movimiento.capture());
        assertThat(movimiento.getValue().tipo()).isEqualTo(TipoMovimiento.RETIRO);
        assertThat(movimiento.getValue().referencia()).isEqualTo("CAJ-001-abc12345");
        assertThat(movimiento.getValue().descripcion()).isEqualTo("Retiro en cajero CAJ-001");
        assertThat(movimiento.getValue().monto()).isEqualByComparingTo("2000");
    }

    @Test
    void rechazaEventosIncompletosSinRegistrarNada() {
        RetiroRealizado retiro = new RetiroRealizado("CAJ-001-abc12346", null, new BigDecimal("2000"), "CAJ-001",
                "CAJERO", LocalDateTime.now());

        assertThatThrownBy(() -> listener.recibir(
                new ConsumerRecord<>(RetiroRealizado.TOPICO, 0, 8L, "x", json.writeValueAsString(retiro))))
                .isInstanceOf(ConstraintViolationException.class);
        verifyNoInteractions(servicio);
    }
}
