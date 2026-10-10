package com.bancoxyz.cuentas_api.controller;

import com.bancoxyz.cuentas_api.model.CuentaBancaria;
import com.bancoxyz.cuentas_api.model.CuentaBancaria.TipoCuenta;
import com.bancoxyz.cuentas_api.service.CuentaBancariaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas-bancarias")
public class CuentasBancariasController {

    private final CuentaBancariaService service;

    public CuentasBancariasController(CuentaBancariaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CuentaBancaria> listar(@RequestParam(required = false) Long clienteId) {
        return service.listar(clienteId);
    }

    @GetMapping("/{id}")
    public CuentaBancaria buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaBancaria abrir(@Valid @RequestBody AperturaCuenta request) {
        return service.abrir(request.clienteId(), request.tipo(), request.saldoInicial());
    }

    @PutMapping("/{id}")
    public CuentaBancaria mantener(@PathVariable Long id, @Valid @RequestBody MantenimientoCuenta request) {
        return service.mantener(id, request.clienteId(), request.tipo());
    }

    @PatchMapping("/{id}/cierre")
    public CuentaBancaria cerrar(@PathVariable Long id) {
        return service.cerrar(id);
    }

    @PostMapping("/transferencias")
    @ResponseStatus(HttpStatus.OK)
    public CuentaBancariaService.TransferenciaResponse transferir(
            @Valid @RequestBody TransferenciaCuenta request) {
        return service.transferir(request.cuentaOrigen(), request.cuentaDestino(),
                request.monto(), request.claveIdempotencia());
    }

    public record AperturaCuenta(
            @NotNull @Positive Long clienteId,
            @NotNull TipoCuenta tipo,
            @NotNull @DecimalMin("0.00") BigDecimal saldoInicial) {
    }

    public record MantenimientoCuenta(
            @NotNull @Positive Long clienteId,
            @NotNull TipoCuenta tipo) {
    }

    public record TransferenciaCuenta(
            @NotNull @Positive Long cuentaOrigen,
            @NotNull @Positive Long cuentaDestino,
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal monto,
            @NotBlank @Size(max = 80) String claveIdempotencia) {
    }
}
