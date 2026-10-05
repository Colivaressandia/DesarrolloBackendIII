package com.bancoxyz.banco_microservicio.service;

import com.bancoxyz.banco_microservicio.event.TransaccionCreadaEvent;
import com.bancoxyz.banco_microservicio.kafka.TransaccionProducer;
import com.bancoxyz.banco_microservicio.model.CuentaAnual;
import com.bancoxyz.banco_microservicio.model.Interes;
import com.bancoxyz.banco_microservicio.model.Transaccion;
import com.bancoxyz.banco_microservicio.repository.CuentaAnualRepository;
import com.bancoxyz.banco_microservicio.repository.InteresRepository;
import com.bancoxyz.banco_microservicio.repository.TransaccionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private final CuentaAnualRepository cuentaRepo;
    private final InteresRepository interesRepo;
    private final TransaccionRepository transaccionRepo;
    private final TransaccionProducer transaccionProducer;

    public BankService(CuentaAnualRepository cuentaRepo,
                       InteresRepository interesRepo,
                       TransaccionRepository transaccionRepo,
                       TransaccionProducer transaccionProducer) {

        this.cuentaRepo = cuentaRepo;
        this.interesRepo = interesRepo;
        this.transaccionRepo = transaccionRepo;
        this.transaccionProducer = transaccionProducer;
    }

    // ---------- CUENTAS ANUALES ----------

    @CircuitBreaker(name = "bankService", fallbackMethod = "fallbackCuentas")
    @Retry(name = "bankService")
    public List<CuentaAnual> listarCuentasAnuales() {
        return cuentaRepo.findAll();
    }

    public List<CuentaAnual> fallbackCuentas(Throwable t) {
        log.warn(
                "Circuit Breaker activado en listarCuentasAnuales: {}",
                t.getMessage()
        );

        return Collections.emptyList();
    }

    @CircuitBreaker(name = "bankService", fallbackMethod = "fallbackCuentasPorId")
    @Retry(name = "bankService")
    public List<CuentaAnual> listarCuentasPorCuentaId(Long cuentaId) {
        return cuentaRepo.findByCuentaId(cuentaId);
    }

    public List<CuentaAnual> fallbackCuentasPorId(Long cuentaId, Throwable t) {
        log.warn(
                "Circuit Breaker en listarCuentasPorCuentaId({}): {}",
                cuentaId,
                t.getMessage()
        );

        return Collections.emptyList();
    }

    // ---------- INTERESES ----------

    @CircuitBreaker(name = "bankService", fallbackMethod = "fallbackIntereses")
    @Retry(name = "bankService")
    public List<Interes> listarIntereses() {
        return interesRepo.findAll();
    }

    public List<Interes> fallbackIntereses(Throwable t) {
        log.warn(
                "Circuit Breaker en listarIntereses: {}",
                t.getMessage()
        );

        return Collections.emptyList();
    }

    // ---------- TRANSACCIONES ----------

    public Transaccion crearTransaccion(Transaccion transaccion) {

        /*
         * Las transacciones migradas utilizan IDs asignados manualmente.
         * Si el POST no proporciona un ID, se obtiene el ID mayor existente
         * y se asigna el siguiente disponible.
         */
        if (transaccion.getId() == null) {

            Long siguienteId = transaccionRepo.findAll()
                    .stream()
                    .map(Transaccion::getId)
                    .filter(id -> id != null)
                    .max(Comparator.naturalOrder())
                    .orElse(0L) + 1L;

            transaccion.setId(siguienteId);

            log.info(
                    "ID {} asignado a la nueva transacción",
                    siguienteId
            );
        }

        // Guarda la nueva transacción
        Transaccion guardada = transaccionRepo.save(transaccion);

        log.info(
                "Transacción {} guardada correctamente",
                guardada.getId()
        );

        // Construye el evento que será enviado a Kafka
        TransaccionCreadaEvent evento = new TransaccionCreadaEvent(
                guardada.getId(),
                guardada.getFecha(),
                guardada.getMonto(),
                guardada.getTipo()
        );

        // Publica el evento de forma asíncrona
        transaccionProducer.publicarEvento(evento);

        log.info(
                "Transacción {} guardada y evento enviado a Kafka",
                guardada.getId()
        );

        return guardada;
    }

    @CircuitBreaker(name = "bankService", fallbackMethod = "fallbackTransacciones")
    @Retry(name = "bankService")
    public List<Transaccion> listarTransacciones() {
        return transaccionRepo.findAll();
    }

    public List<Transaccion> fallbackTransacciones(Throwable t) {

        log.warn(
                "Circuit Breaker activado en listarTransacciones: {}",
                t.getMessage()
        );

        Transaccion fallback = new Transaccion();
        fallback.setId(-1L);
        fallback.setFecha("N/A");
        fallback.setMonto(0.0);
        fallback.setTipo("FALLBACK - servicio de transacciones no disponible");

        return List.of(fallback);
    }

    @CircuitBreaker(name = "bankService", fallbackMethod = "fallbackTransaccionPorId")
    @Retry(name = "bankService")
    public Transaccion obtenerTransaccion(Long id) {

        return transaccionRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Transacción no encontrada: " + id
                        )
                );
    }

    public Transaccion fallbackTransaccionPorId(Long id, Throwable t) {

        log.warn(
                "Circuit Breaker en obtenerTransaccion({}): {}",
                id,
                t.getMessage()
        );

        Transaccion fallback = new Transaccion();
        fallback.setId(-1L);
        fallback.setFecha("N/A");
        fallback.setMonto(0.0);
        fallback.setTipo(
                "FALLBACK - transacción no disponible temporalmente"
        );

        return fallback;
    }
}