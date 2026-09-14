package cl.duoc.bancoxyz.legacy.service;

import cl.duoc.bancoxyz.legacy.model.CuentaEntity;
import cl.duoc.bancoxyz.legacy.model.MovimientoAnualEntity;
import cl.duoc.bancoxyz.legacy.repository.CuentaRepository;
import cl.duoc.bancoxyz.legacy.repository.MovimientoAnualRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class LegacyCoreService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoAnualRepository movimientoRepository;

    public LegacyCoreService(CuentaRepository cuentaRepository,
                             MovimientoAnualRepository movimientoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public List<CuentaEntity> obtenerCuentasPorCliente(String nombre) {
        return cuentaRepository.findByNombreIgnoreCase(nombre);
    }

    public Optional<CuentaEntity> obtenerCuentaPorId(Long cuentaId) {
        return cuentaRepository.findFirstByCuentaId(cuentaId);
    }

    public List<MovimientoAnualEntity> obtenerMovimientosPorCuenta(Long cuentaId) {
        return movimientoRepository.findByCuentaIdOrderByFechaDesc(cuentaId);
    }

    public CuentaEntity procesarRetiro(Long cuentaId, BigDecimal monto) {
        CuentaEntity cuenta = cuentaRepository.findFirstByCuentaId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta ID " + cuentaId + " no existe."));

        if (cuenta.getSaldo().compareTo(monto) < 0) {
            throw new IllegalStateException("Saldo insuficiente para realizar el retiro solicitado.");
        }

        cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        return cuentaRepository.save(cuenta);
    }
}