package com.bancoxyz.intereses_api.service;

import com.bancoxyz.intereses_api.model.Interes;
import com.bancoxyz.intereses_api.repository.InteresRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class InteresesService {

    private static final Logger log = LoggerFactory.getLogger(InteresesService.class);

    private final InteresRepository interesRepo;

    public InteresesService(InteresRepository interesRepo) {
        this.interesRepo = interesRepo;
    }

    @CircuitBreaker(name = "interesesService", fallbackMethod = "fallbackIntereses")
    @Retry(name = "interesesService")
    public List<Interes> listarIntereses() {
        return interesRepo.findAll();
    }

    public List<Interes> fallbackIntereses(Throwable t) {
        log.warn("Circuit Breaker activado en listarIntereses: {}", t.getMessage());
        Interes fallback = new Interes();
        fallback.setId(-1L);
        fallback.setNombre("FALLBACK");
        fallback.setSaldo(0.0);
        fallback.setEdad(0);
        fallback.setTipo("Servicio de intereses no disponible");
        return List.of(fallback);
    }

    @CircuitBreaker(name = "interesesService", fallbackMethod = "fallbackPorCuentaId")
    @Retry(name = "interesesService")
    public List<Interes> listarPorCuentaId(Long cuentaId) {
        return interesRepo.findByCuentaId(cuentaId);
    }

    public List<Interes> fallbackPorCuentaId(Long cuentaId, Throwable t) {
        log.warn("Circuit Breaker en listarPorCuentaId({}): {}", cuentaId, t.getMessage());
        return Collections.emptyList();
    }

    @CircuitBreaker(name = "interesesService", fallbackMethod = "fallbackPorTipo")
    @Retry(name = "interesesService")
    public List<Interes> listarPorTipo(String tipo) {
        return interesRepo.findByTipo(tipo);
    }

    public List<Interes> fallbackPorTipo(String tipo, Throwable t) {
        log.warn("Circuit Breaker en listarPorTipo({}): {}", tipo, t.getMessage());
        return Collections.emptyList();
    }
}