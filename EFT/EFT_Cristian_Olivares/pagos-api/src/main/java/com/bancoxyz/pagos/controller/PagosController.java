package com.bancoxyz.pagos.controller;

import com.bancoxyz.pagos.model.Pago;
import com.bancoxyz.pagos.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagosController {

    private final PagoService service;

    public PagosController(PagoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Pago> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Pago buscar(@PathVariable String id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Pago crear(@Valid @RequestBody PagoRequest request,
                      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return service.crear(request, authorization);
    }
}
