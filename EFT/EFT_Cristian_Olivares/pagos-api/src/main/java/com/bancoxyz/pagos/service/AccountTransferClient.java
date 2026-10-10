package com.bancoxyz.pagos.service;

import com.bancoxyz.pagos.controller.PagoRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class AccountTransferClient {

    private final RestClient client;

    public AccountTransferClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        this.client = builder.build();
    }

    @Retry(name = "accountTransfer")
    @CircuitBreaker(name = "accountTransfer", fallbackMethod = "transferirFallback")
    public void transferir(PagoRequest request, String authorization) {
        TransferResponse response = client.post()
                .uri("http://cuentas-api/api/v1/cuentas-bancarias/transferencias")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(new AccountTransferRequest(request.cuentaOrigen(), request.cuentaDestino(),
                        request.monto(), request.claveIdempotencia()))
                .retrieve()
                .body(TransferResponse.class);
        if (response == null) {
            throw new ResourceAccessException("La API de cuentas devolvió una respuesta vacía");
        }
    }

    private void transferirFallback(PagoRequest request, String authorization, Throwable error) {
        if (error instanceof RestClientResponseException responseException) {
            throw new ResponseStatusException(responseException.getStatusCode(),
                    "La transferencia fue rechazada por el servicio de cuentas");
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de cuentas no está disponible; reintenta con la misma clave de idempotencia",
                error);
    }

    private record AccountTransferRequest(Long cuentaOrigen, Long cuentaDestino, BigDecimal monto,
                                           String claveIdempotencia) {
    }

    private record TransferResponse(String claveIdempotencia, boolean repetida) {
    }
}
