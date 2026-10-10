package com.bancoxyz.banco_microservicio.controller;

import com.bancoxyz.banco_microservicio.model.CuentaAnual;
import com.bancoxyz.banco_microservicio.model.Interes;
import com.bancoxyz.banco_microservicio.model.Transaccion;
import com.bancoxyz.banco_microservicio.service.BankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    // ---------- CUENTAS ANUALES ----------

    @GetMapping("/cuentas-anuales")
    public List<CuentaAnual> cuentasAnuales() {
        return bankService.listarCuentasAnuales();
    }

    @GetMapping("/cuentas-anuales/{cuentaId}")
    public List<CuentaAnual> cuentasPorId(@PathVariable Long cuentaId) {
        return bankService.listarCuentasPorCuentaId(cuentaId);
    }

    // ---------- INTERESES ----------

    @GetMapping("/intereses")
    public List<Interes> intereses() {
        return bankService.listarIntereses();
    }

    // ---------- TRANSACCIONES ----------

    @PostMapping("/transacciones")
    public ResponseEntity<Transaccion> crearTransaccion(
            @RequestBody Transaccion transaccion) {

        Transaccion creada = bankService.crearTransaccion(transaccion);

        return ResponseEntity.ok(creada);
    }

    @GetMapping("/transacciones")
    public List<Transaccion> transacciones() {
        return bankService.listarTransacciones();
    }

    @GetMapping("/transacciones/{id}")
    public ResponseEntity<Transaccion> transaccion(@PathVariable Long id) {

        Transaccion t = bankService.obtenerTransaccion(id);

        return t != null
                ? ResponseEntity.ok(t)
                : ResponseEntity.notFound().build();
    }
}