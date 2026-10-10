package com.bancoxyz.cuentas_api.service;

import com.bancoxyz.cuentas_api.model.CuentaAnual;
import com.bancoxyz.cuentas_api.repository.CuentaAnualRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CuentasService {

    private static final Logger log = LoggerFactory.getLogger(CuentasService.class);

    private final CuentaAnualRepository cuentaRepo;

    public CuentasService(CuentaAnualRepository cuentaRepo) {
        this.cuentaRepo = cuentaRepo;
    }

    @CircuitBreaker(name = "cuentasService", fallbackMethod = "fallbackCuentas")
    @Retry(name = "cuentasService")
    public List<CuentaAnual> listarCuentas() {
        return cuentaRepo.findAll();
    }

    public List<CuentaAnual> fallbackCuentas(Throwable t) {
        log.warn("Circuit Breaker activado en listarCuentas: {}", t.getMessage());
        CuentaAnual fallback = new CuentaAnual();
        fallback.setId(-1L);
        fallback.setFecha("N/A");
        fallback.setTransaccion("FALLBACK");
        fallback.setMonto(0.0);
        fallback.setDescripcion("Servicio de cuentas no disponible");
        return List.of(fallback);
    }

    @CircuitBreaker(name = "cuentasService", fallbackMethod = "fallbackCuentasPorId")
    @Retry(name = "cuentasService")
    public List<CuentaAnual> listarPorCuentaId(Long cuentaId) {
        return cuentaRepo.findByCuentaId(cuentaId);
    }

    public List<CuentaAnual> fallbackCuentasPorId(Long cuentaId, Throwable t) {
        log.warn("Circuit Breaker en listarPorCuentaId({}): {}", cuentaId, t.getMessage());
        return Collections.emptyList();
    }
}