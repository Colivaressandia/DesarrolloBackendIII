package com.bancoxyz.bffmobile;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/bff/mobile")
public class MobileBffController {

    private final RestClient client;

    public MobileBffController(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        this.client = builder.build();
    }

    @GetMapping("/clientes/{clienteId}/resumen")
    public Map<String, Object> resumen(@PathVariable Long clienteId,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        JsonNode cliente = client.get()
                .uri("http://clientes-api/api/v1/clientes/{id}", clienteId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(JsonNode.class);
        JsonNode cuentas = client.get()
                .uri("http://cuentas-api/api/v1/cuentas-bancarias?clienteId={id}", clienteId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(JsonNode.class);
        List<Map<String, Object>> cuentasLivianas = StreamSupport.stream(cuentas.spliterator(), false)
                .map(cuenta -> Map.<String, Object>of(
                        "id", cuenta.path("id").asLong(),
                        "tipo", cuenta.path("tipo").asText(),
                        "saldo", cuenta.path("saldo").decimalValue(),
                        "estado", cuenta.path("estado").asText()))
                .toList();

        return Map.of(
                "clienteId", cliente.path("id").asLong(),
                "nombre", cliente.path("nombres").asText() + " " + cliente.path("apellidos").asText(),
                "cuentas", cuentasLivianas);
    }
}
