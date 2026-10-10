package com.bancoxyz.pagos.service;

import com.bancoxyz.pagos.model.EventoOutbox;
import com.bancoxyz.pagos.model.PagoProcesadoEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class KafkaEventTransport {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventTransport.class);

    private final KafkaTemplate<String, PagoProcesadoEvent> kafkaTemplate;
    private final String topic;
    private final long timeoutSeconds;

    public KafkaEventTransport(
            KafkaTemplate<String, PagoProcesadoEvent> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic,
            @Value("${app.kafka.publish-timeout-seconds:3}") long timeoutSeconds) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Retry(name = "paymentEventPublisher")
    @CircuitBreaker(name = "paymentEventPublisher", fallbackMethod = "fallback")
    public boolean publish(EventoOutbox event) {
        try {
            kafkaTemplate.send(topic, event.getPagoId(), PagoProcesadoEvent.from(event))
                    .get(timeoutSeconds, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación Kafka interrumpida", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("No se pudo confirmar el evento Kafka", ex);
        }
    }

    private boolean fallback(EventoOutbox event, Throwable error) {
        log.error("El evento {} permanece en outbox tras fallar Kafka: {}",
                event.getEventoId(), error.getMessage());
        return false;
    }
}
