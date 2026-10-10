package com.bancoxyz.cuentas_api.service;

import com.bancoxyz.cuentas_api.model.CuentaBancaria;
import com.bancoxyz.cuentas_api.model.CuentaBancaria.TipoCuenta;
import com.bancoxyz.cuentas_api.model.TransferenciaProcesada;
import com.bancoxyz.cuentas_api.repository.CuentaBancariaRepository;
import com.bancoxyz.cuentas_api.repository.TransferenciaProcesadaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class CuentaBancariaService {

    private final CuentaBancariaRepository repository;
    private final TransferenciaProcesadaRepository transferenciaRepository;

    public CuentaBancariaService(CuentaBancariaRepository repository,
                                 TransferenciaProcesadaRepository transferenciaRepository) {
        this.repository = repository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional(readOnly = true)
    public List<CuentaBancaria> listar(Long clienteId) {
        return clienteId == null ? repository.findAll() : repository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public CuentaBancaria buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));
    }

    public CuentaBancaria abrir(Long clienteId, TipoCuenta tipo, BigDecimal saldoInicial) {
        if (clienteId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clienteId debe ser positivo");
        }
        if (saldoInicial.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El saldo inicial no puede ser negativo");
        }
        return repository.save(new CuentaBancaria(clienteId, tipo, saldoInicial));
    }

    public CuentaBancaria mantener(Long id, Long clienteId, TipoCuenta tipo) {
        CuentaBancaria cuenta = buscar(id);
        if (cuenta.getEstado() == CuentaBancaria.EstadoCuenta.CERRADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar una cuenta cerrada");
        }
        if (clienteId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clienteId debe ser positivo");
        }
        cuenta.setClienteId(clienteId);
        cuenta.setTipo(tipo);
        return repository.save(cuenta);
    }

    public CuentaBancaria cerrar(Long id) {
        CuentaBancaria cuenta = buscar(id);
        cuenta.cerrar();
        return repository.save(cuenta);
    }

    public TransferenciaResponse transferir(Long cuentaOrigenId, Long cuentaDestinoId,
                                            BigDecimal monto, String claveIdempotencia) {
        if (monto == null || monto.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser positivo");
        }
        if (claveIdempotencia == null || claveIdempotencia.isBlank()
                || claveIdempotencia.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave de idempotencia no es válida");
        }
        if (cuentaOrigenId.equals(cuentaDestinoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las cuentas de origen y destino deben ser distintas");
        }
        List<Long> idsOrdenados = List.of(cuentaOrigenId, cuentaDestinoId).stream()
                .sorted(Comparator.naturalOrder()).toList();
        CuentaBancaria primera = buscarConBloqueo(idsOrdenados.get(0));
        CuentaBancaria segunda = buscarConBloqueo(idsOrdenados.get(1));
        var transferenciaExistente = transferenciaRepository.findByClaveIdempotencia(claveIdempotencia);
        if (transferenciaExistente.isPresent()) {
            TransferenciaProcesada transferencia = transferenciaExistente.get();
            boolean mismaOperacion = transferencia.getCuentaOrigen().equals(cuentaOrigenId)
                    && transferencia.getCuentaDestino().equals(cuentaDestinoId)
                    && transferencia.getMonto().compareTo(monto) == 0;
            if (!mismaOperacion) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La clave de idempotencia ya se usó con otra transferencia");
            }
            return new TransferenciaResponse(claveIdempotencia, true);
        }
        CuentaBancaria origen = primera.getId().equals(cuentaOrigenId) ? primera : segunda;
        CuentaBancaria destino = primera.getId().equals(cuentaDestinoId) ? primera : segunda;

        if (origen.getEstado() != CuentaBancaria.EstadoCuenta.ACTIVA
                || destino.getEstado() != CuentaBancaria.EstadoCuenta.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ambas cuentas deben estar activas");
        }
        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo insuficiente");
        }

        origen.debitar(monto);
        destino.abonar(monto);
        repository.saveAll(List.of(origen, destino));
        transferenciaRepository.save(new TransferenciaProcesada(
                claveIdempotencia, cuentaOrigenId, cuentaDestinoId, monto));
        return new TransferenciaResponse(claveIdempotencia, false);
    }

    private CuentaBancaria buscarConBloqueo(Long id) {
        return repository.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));
    }

    public record TransferenciaResponse(String claveIdempotencia, boolean repetida) {
    }
}
