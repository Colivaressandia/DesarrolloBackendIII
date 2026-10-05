package com.bancoxyz.intereses_api.controller;

import com.bancoxyz.intereses_api.model.Interes;
import com.bancoxyz.intereses_api.service.InteresesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InteresesController {

    private final InteresesService interesesService;

    public InteresesController(InteresesService interesesService) {
        this.interesesService = interesesService;
    }

    @GetMapping("/intereses")
    public List<Interes> intereses() {
        return interesesService.listarIntereses();
    }

    @GetMapping("/intereses/cuenta/{cuentaId}")
    public List<Interes> porCuenta(@PathVariable Long cuentaId) {
        return interesesService.listarPorCuentaId(cuentaId);
    }

    @GetMapping("/intereses/tipo/{tipo}")
    public List<Interes> porTipo(@PathVariable String tipo) {
        return interesesService.listarPorTipo(tipo);
    }
}