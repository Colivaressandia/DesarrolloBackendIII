package com.bancoxyz.cuentas_api.controller;

import com.bancoxyz.cuentas_api.model.CuentaAnual;
import com.bancoxyz.cuentas_api.service.CuentasService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CuentasController {

    private final CuentasService cuentasService;

    public CuentasController(CuentasService cuentasService) {
        this.cuentasService = cuentasService;
    }

    @GetMapping("/cuentas")
    public List<CuentaAnual> cuentas() {
        return cuentasService.listarCuentas();
    }

    @GetMapping("/cuentas/{cuentaId}")
    public List<CuentaAnual> cuentasPorId(@PathVariable Long cuentaId) {
        return cuentasService.listarPorCuentaId(cuentaId);
    }
}