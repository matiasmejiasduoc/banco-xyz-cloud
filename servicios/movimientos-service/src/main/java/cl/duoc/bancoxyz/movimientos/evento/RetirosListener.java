package cl.duoc.bancoxyz.movimientos.evento;

import cl.duoc.bancoxyz.commons.evento.RetiroRealizado;
import cl.duoc.bancoxyz.movimientos.api.MovimientoRespuesta;
import cl.duoc.bancoxyz.movimientos.api.MovimientoService;
import cl.duoc.bancoxyz.movimientos.api.NuevoMovimiento;
import cl.duoc.bancoxyz.movimientos.dominio.TipoMovimiento;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class RetirosListener {

    private static final Logger log = LoggerFactory.getLogger(RetirosListener.class);

    private final MovimientoService servicio;
    private final JsonMapper json;
    private final Validator validador;

    public RetirosListener(MovimientoService servicio, JsonMapper json, Validator validador) {
        this.servicio = servicio;
        this.json = json;
        this.validador = validador;
    }

    @KafkaListener(topics = RetiroRealizado.TOPICO, groupId = "${spring.application.name}")
    public void recibir(ConsumerRecord<String, String> registro) {
        RetiroRealizado retiro = json.readValue(registro.value(), RetiroRealizado.class);
        NuevoMovimiento movimiento = new NuevoMovimiento(retiro.cuentaId(), TipoMovimiento.RETIRO, retiro.monto(),
                "Retiro en cajero " + retiro.terminal(), retiro.canal(), retiro.referencia());

        Set<ConstraintViolation<NuevoMovimiento>> errores = validador.validate(movimiento);
        if (!errores.isEmpty()) {
            throw new ConstraintViolationException("Evento de retiro invalido " + retiro.referencia(), errores);
        }

        MovimientoRespuesta registrado = servicio.registrar(movimiento);
        log.info("Retiro {} registrado como movimiento {} (particion {}, offset {})", retiro.referencia(),
                registrado.id(), registro.partition(), registro.offset());
    }
}
