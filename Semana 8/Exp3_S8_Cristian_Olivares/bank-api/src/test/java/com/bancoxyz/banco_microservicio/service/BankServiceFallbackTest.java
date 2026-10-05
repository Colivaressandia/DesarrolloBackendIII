package com.bancoxyz.banco_microservicio.service;

import com.bancoxyz.banco_microservicio.model.Transaccion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BankServiceFallbackTest {

    @Test
    void fallbackTransaccionPorIdReturnsAnIdentifiableFallback() {
        BankService service = new BankService(null, null, null, null);

        Transaccion fallback = service.fallbackTransaccionPorId(
                42L,
                new RuntimeException("service unavailable")
        );

        assertNotNull(fallback);
        assertEquals(-1L, fallback.getId());
        assertEquals(
                "FALLBACK - transacción no disponible temporalmente",
                fallback.getTipo()
        );
    }
}
