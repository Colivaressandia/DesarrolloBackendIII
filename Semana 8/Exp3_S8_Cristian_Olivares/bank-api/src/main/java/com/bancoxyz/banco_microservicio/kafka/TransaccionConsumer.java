package com.bancoxyz.banco_microservicio.kafka;

import com.bancoxyz.banco_microservicio.event.TransaccionCreadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class TransaccionConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(TransaccionConsumer.class);

    @KafkaListener(
            topics = "transacciones-creadas",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumirEvento(
            TransaccionCreadaEvent evento,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {

        log.info(
                "EVENTO RECIBIDO DESDE KAFKA -> partición: {} | consumidor: {} | transacción: {} | fecha: {} | monto: {} | tipo: {}",
                partition,
                Thread.currentThread().getName(),
                evento.getTransaccionId(),
                evento.getFecha(),
                evento.getMonto(),
                evento.getTipo()
        );
    }
}