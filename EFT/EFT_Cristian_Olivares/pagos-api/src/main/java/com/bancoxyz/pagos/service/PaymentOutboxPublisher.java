package com.bancoxyz.pagos.service;

import com.bancoxyz.pagos.repository.EventoOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxPublisher.class);

    private final EventoOutboxRepository outboxRepository;
    private final KafkaEventTransport eventTransport;

    public PaymentOutboxPublisher(EventoOutboxRepository outboxRepository,
                                 KafkaEventTransport eventTransport) {
        this.outboxRepository = outboxRepository;
        this.eventTransport = eventTransport;
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-milliseconds:5000}")
    @Transactional
    public void publishPendingEvents() {
        var pendingEvents = outboxRepository.findTop100ByPublicadoEnIsNullOrderByCreadoEnAsc();
        for (var event : pendingEvents) {
            if (eventTransport.publish(event)) {
                event.marcarPublicado();
            } else {
                event.registrarIntentoFallido();
                log.warn("Evento {} pendiente; reintento acumulado: {}",
                        event.getEventoId(), event.getIntentos());
            }
        }
    }
}
