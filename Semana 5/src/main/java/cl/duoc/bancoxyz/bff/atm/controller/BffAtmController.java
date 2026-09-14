package cl.duoc.bancoxyz.bff.atm.controller;

import cl.duoc.bancoxyz.bff.atm.dto.AtmConsultaSaldoResponse;
import cl.duoc.bancoxyz.bff.atm.dto.AtmRetiroRequest;
import cl.duoc.bancoxyz.bff.atm.dto.AtmRetiroResponse;
import cl.duoc.bancoxyz.bff.atm.service.BffAtmService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff/atm")
public class BffAtmController {

    private final BffAtmService atmService;

    public BffAtmController(BffAtmService atmService) {
        this.atmService = atmService;
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public ResponseEntity<AtmConsultaSaldoResponse> consultarSaldo(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(atmService.consultarSaldo(cuentaId));
    }

    @PostMapping("/retiro")
    public ResponseEntity<AtmRetiroResponse> realizarRetiro(@Valid @RequestBody AtmRetiroRequest request) {
        return ResponseEntity.ok(atmService.realizarRetiro(request));
    }
}