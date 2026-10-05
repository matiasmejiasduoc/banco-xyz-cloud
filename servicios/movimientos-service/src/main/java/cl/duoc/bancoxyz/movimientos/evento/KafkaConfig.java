package cl.duoc.bancoxyz.movimientos.evento;

import cl.duoc.bancoxyz.commons.error.ConflictoException;
import cl.duoc.bancoxyz.commons.evento.RetiroRealizado;
import jakarta.validation.ConstraintViolationException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import tools.jackson.core.JacksonException;

@Configuration
public class KafkaConfig {

    public static final String TOPICO_FALLIDOS = RetiroRealizado.TOPICO + ".DLT";

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    NewTopic topicoRetiros() {
        return TopicBuilder.name(RetiroRealizado.TOPICO).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic topicoRetirosFallidos() {
        return TopicBuilder.name(TOPICO_FALLIDOS).partitions(3).replicas(1).build();
    }

    @Bean
    DefaultErrorHandler manejadorErroresKafka(KafkaOperations<Object, Object> kafka,
            @Value("${banco.eventos.reintentos}") int reintentos) {
        DeadLetterPublishingRecoverer haciaDlt = new DeadLetterPublishingRecoverer(kafka,
                (registro, error) -> new TopicPartition(TOPICO_FALLIDOS, registro.partition()));
        ExponentialBackOff espera = new ExponentialBackOff(1000, 2);
        espera.setMaxAttempts(reintentos);
        DefaultErrorHandler manejador = new DefaultErrorHandler((registro, error) -> {
            log.error("Evento {} enviado a {}: {}", registro.value(), TOPICO_FALLIDOS,
                    NestedExceptionUtils.getMostSpecificCause(error).getMessage());
            haciaDlt.accept(registro, error);
        }, espera);
        manejador.addNotRetryableExceptions(JacksonException.class, ConstraintViolationException.class,
                ConflictoException.class);
        return manejador;
    }
}
