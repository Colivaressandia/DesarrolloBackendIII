package com.bancoxyz.pagos.service;

import com.bancoxyz.pagos.model.PagoProcesadoEvent;
import com.bancoxyz.pagos.repository.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PagoRepository pagoRepository;

    public PaymentEventConsumer(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void onPaymentProcessed(PagoProcesadoEvent event) {
        var pago = pagoRepository.findById(event.pagoId())
                .orElseThrow(() -> new IllegalStateException("El evento referencia un pago inexistente"));
        pago.marcarProcesado();
        log.info("Pago procesado desde Kafka: pagoId={}, eventoId={}", event.pagoId(), event.eventoId());
    }
}
