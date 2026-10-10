package com.bancoxyz.banco_microservicio.service;

import com.bancoxyz.banco_microservicio.repository.TransaccionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false",
        "resilience4j.retry.instances.bankService.max-attempts=3",
        "resilience4j.retry.instances.bankService.wait-duration=2s",
        "resilience4j.retry.instances.bankService.retry-exceptions[0]=java.io.IOException",
        "resilience4j.circuitbreaker.circuit-breaker-aspect-order=1",
        "resilience4j.retry.retry-aspect-order=2"
})
class BankServiceRetryTest {

    @Autowired
    private BankService bankService;

    @MockitoBean
    private TransaccionRepository transaccionRepository;

    @Test
    void listarTransaccionesRetriesTransientIoFailuresAndRecovers() {
        AtomicInteger attempts = new AtomicInteger();

        doAnswer(invocation -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IOException("temporary database connection failure");
            }
            return List.of();
        }).when(transaccionRepository).findAll();

        List<?> transactions = bankService.listarTransacciones();

        assertEquals(3, attempts.get());
        assertTrue(transactions.isEmpty());
        System.out.printf(
                "Retry verificado: %d intentos; recuperacion exitosa al tercer intento.%n",
                attempts.get()
        );
    }
}
