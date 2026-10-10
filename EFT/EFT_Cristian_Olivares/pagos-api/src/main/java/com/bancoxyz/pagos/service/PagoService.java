package com.bancoxyz.pagos.service;

import com.bancoxyz.pagos.controller.PagoRequest;
import com.bancoxyz.pagos.model.EventoOutbox;
import com.bancoxyz.pagos.model.Pago;
import com.bancoxyz.pagos.repository.EventoOutboxRepository;
import com.bancoxyz.pagos.repository.PagoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final EventoOutboxRepository outboxRepository;
    private final AccountTransferClient accountTransferClient;

    public PagoService(PagoRepository pagoRepository, EventoOutboxRepository outboxRepository,
                       AccountTransferClient accountTransferClient) {
        this.pagoRepository = pagoRepository;
        this.outboxRepository = outboxRepository;
        this.accountTransferClient = accountTransferClient;
    }

    @Transactional(readOnly = true)
    public List<Pago> listar() {
        return pagoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pago buscar(String id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
    }

    public Pago crear(PagoRequest request, String authorization) {
        if (request.monto() == null || request.monto().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser positivo");
        }
        if (request.claveIdempotencia() == null || request.claveIdempotencia().isBlank()
                || request.claveIdempotencia().length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave de idempotencia no es válida");
        }
        if (request.cuentaOrigen().equals(request.cuentaDestino())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las cuentas de origen y destino deben ser distintas");
        }
        if (!request.moneda().equals("CLP")) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Las cuentas actuales solo admiten transferencias en CLP");
        }
        var existente = pagoRepository.findByClaveIdempotencia(request.claveIdempotencia());
        if (existente.isPresent()) {
            Pago pago = existente.get();
            boolean mismaOperacion = pago.getCuentaOrigen().equals(request.cuentaOrigen())
                    && pago.getCuentaDestino().equals(request.cuentaDestino())
                    && pago.getMonto().compareTo(request.monto()) == 0
                    && pago.getMoneda().equals(request.moneda());
            if (!mismaOperacion) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La clave de idempotencia ya se usó con datos distintos");
            }
            return pago;
        }

        accountTransferClient.transferir(request, authorization);
        Pago pago = pagoRepository.save(new Pago(UUID.randomUUID().toString(),
                request.cuentaOrigen(), request.cuentaDestino(), request.monto(),
                request.moneda(), request.claveIdempotencia()));
        outboxRepository.save(new EventoOutbox(UUID.randomUUID().toString(), pago));
        return pago;
    }
}
