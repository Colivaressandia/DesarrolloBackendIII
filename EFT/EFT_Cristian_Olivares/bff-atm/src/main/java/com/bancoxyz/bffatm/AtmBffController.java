package com.bancoxyz.bffatm;

import jakarta.validation.Valid;
import tools.jackson.databind.JsonNode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bff/atm")
public class AtmBffController {

    private final RestClient client;

    public AtmBffController(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        this.client = builder.build();
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public Map<String, Object> saldo(@PathVariable Long cuentaId,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        JsonNode account = client.get()
                .uri("http://cuentas-api/api/v1/cuentas-bancarias/{id}", cuentaId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(JsonNode.class);
        return Map.of(
                "cuentaId", account.path("id").asLong(),
                "saldo", account.path("saldo").decimalValue(),
                "estado", account.path("estado").asText());
    }

    @PostMapping("/transferencias")
    public JsonNode transferir(@Valid @RequestBody TransferenciaRequest request,
                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return client.post()
                .uri("http://pagos-api/api/v1/pagos")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(new PaymentRequest(request.cuentaOrigen(), request.cuentaDestino(),
                        request.monto(), request.moneda(), request.claveIdempotencia()))
                .retrieve()
                .body(JsonNode.class);
    }

    public record TransferenciaRequest(
            @NotNull @Positive Long cuentaOrigen,
            @NotNull @Positive Long cuentaDestino,
            @NotNull @DecimalMin("0.01") BigDecimal monto,
            @NotNull @Pattern(regexp = "[A-Z]{3}") String moneda,
            @NotBlank @Size(max = 80) String claveIdempotencia) {
    }

    private record PaymentRequest(Long cuentaOrigen, Long cuentaDestino, BigDecimal monto,
                                  String moneda, String claveIdempotencia) {
    }
}
