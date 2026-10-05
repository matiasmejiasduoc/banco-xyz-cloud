package cl.duoc.bancoxyz.bff.cajero.evento;

import cl.duoc.bancoxyz.bff.cajero.config.CajeroProperties;
import cl.duoc.bancoxyz.commons.evento.RetiroRealizado;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PublicadorRetiros {

    private static final Logger log = LoggerFactory.getLogger(PublicadorRetiros.class);

    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;
    private final Duration espera;

    public PublicadorRetiros(KafkaTemplate<String, String> kafka, JsonMapper json, CajeroProperties propiedades) {
        this.kafka = kafka;
        this.json = json;
        this.espera = propiedades.esperaPublicacion();
    }

    public void publicar(RetiroRealizado retiro) {
        try {
            SendResult<String, String> resultado = kafka
                    .send(RetiroRealizado.TOPICO, String.valueOf(retiro.cuentaId()), json.writeValueAsString(retiro))
                    .get(espera.toMillis(), TimeUnit.MILLISECONDS);
            log.info("Evento {} publicado en {}-{} offset {}", retiro.referencia(),
                    resultado.getRecordMetadata().topic(), resultado.getRecordMetadata().partition(),
                    resultado.getRecordMetadata().offset());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PublicacionFallidaException(retiro.referencia(), e);
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            throw new PublicacionFallidaException(retiro.referencia(), e);
        }
    }
}
