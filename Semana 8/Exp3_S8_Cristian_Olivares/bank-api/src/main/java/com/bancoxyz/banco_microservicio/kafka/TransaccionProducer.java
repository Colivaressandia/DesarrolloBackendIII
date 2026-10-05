package com.bancoxyz.banco_microservicio.kafka;

import com.bancoxyz.banco_microservicio.event.TransaccionCreadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransaccionProducer {

    private static final Logger log = LoggerFactory.getLogger(TransaccionProducer.class);

    public static final String TOPIC = "transacciones-creadas";

    private final KafkaTemplate<String, TransaccionCreadaEvent> kafkaTemplate;

    public TransaccionProducer(KafkaTemplate<String, TransaccionCreadaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarEvento(TransaccionCreadaEvent evento) {
        kafkaTemplate.send(TOPIC, evento.getTransaccionId().toString(), evento);

        log.info(
                "Evento enviado a Kafka -> tópico: {} | transacción: {} | monto: {} | tipo: {}",
                TOPIC,
                evento.getTransaccionId(),
                evento.getMonto(),
                evento.getTipo()
        );
    }
}