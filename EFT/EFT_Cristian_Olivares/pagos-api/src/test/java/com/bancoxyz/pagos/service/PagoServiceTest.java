package com.bancoxyz.pagos.service;

import com.bancoxyz.pagos.controller.PagoRequest;
import com.bancoxyz.pagos.repository.EventoOutboxRepository;
import com.bancoxyz.pagos.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DataJpaTest
@Import(PagoService.class)
class PagoServiceTest {

    @Autowired
    private PagoService service;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private EventoOutboxRepository outboxRepository;

    @MockitoBean
    private AccountTransferClient accountTransferClient;

    @Test
    void persistsPaymentAndOutboxAtomicallyAndSupportsIdempotentRetries() {
        PagoRequest request = new PagoRequest(10L, 20L, new BigDecimal("125.00"),
                "CLP", "request-123");

        var created = service.crear(request, "Bearer test-token");
        var retry = service.crear(request, "Bearer test-token");

        assertThat(retry.getId()).isEqualTo(created.getId());
        assertThat(pagoRepository.count()).isEqualTo(1);
        assertThat(outboxRepository.count()).isEqualTo(1);
        assertThat(outboxRepository.findAll().get(0).getPagoId()).isEqualTo(created.getId());
        verify(accountTransferClient, times(1)).transferir(request, "Bearer test-token");
    }

    @Test
    void rejectsReusingAnIdempotencyKeyForDifferentPaymentDetails() {
        service.crear(new PagoRequest(10L, 20L, new BigDecimal("125.00"),
                "CLP", "request-456"), "Bearer test-token");

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.crear(new PagoRequest(10L, 20L,
                        new BigDecimal("126.00"), "CLP", "request-456"), "Bearer test-token"))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void rejectsTransferToTheSameAccount() {
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.crear(new PagoRequest(10L, 10L,
                        new BigDecimal("100.00"), "CLP", "request-789"), "Bearer test-token"))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void rejectsUnsupportedCurrencyBeforeCallingAccountsService() {
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.crear(new PagoRequest(10L, 20L,
                        new BigDecimal("100.00"), "USD", "request-usd"), "Bearer test-token"))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(422));

        verifyNoInteractions(accountTransferClient);
    }
}
