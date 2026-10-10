package com.bancoxyz.bffweb;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bff/web")
public class WebBffController {

    private final RestClient client;

    public WebBffController(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        this.client = builder.build();
    }

    @GetMapping("/clientes/{clienteId}/dashboard")
    public Map<String, JsonNode> dashboard(@PathVariable Long clienteId,
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
        return Map.of("cliente", cliente, "cuentas", cuentas);
    }
}
