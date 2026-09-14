package cl.duoc.bancoxyz.bff.atm.service;

import cl.duoc.bancoxyz.bff.atm.dto.AtmConsultaSaldoResponse;
import cl.duoc.bancoxyz.bff.atm.dto.AtmRetiroRequest;
import cl.duoc.bancoxyz.bff.atm.dto.AtmRetiroResponse;
import cl.duoc.bancoxyz.legacy.model.CuentaEntity;
import cl.duoc.bancoxyz.legacy.service.LegacyCoreService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BffAtmService {

    private final LegacyCoreService coreService;

    public BffAtmService(LegacyCoreService coreService) {
        this.coreService = coreService;
    }

    public AtmConsultaSaldoResponse consultarSaldo(Long cuentaId) {
        CuentaEntity cuenta = coreService.obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no existe en el sistema"));

        return AtmConsultaSaldoResponse.builder()
                .cuentaId(cuenta.getCuentaId())
                .saldoDisponible(cuenta.getSaldo())
                .moneda("CLP")
                .timestampOperacion(LocalDateTime.now())
                .build();
    }

    public AtmRetiroResponse realizarRetiro(AtmRetiroRequest request) {
        if (request.getMonto().remainder(new BigDecimal("1000")).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("El cajero solo dispensa múltiplos de 1000");
        }

        CuentaEntity cuentaActualizada = coreService.procesarRetiro(request.getCuentaId(), request.getMonto());

        return AtmRetiroResponse.builder()
                .codigoAutorizacion("AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .cuentaId(cuentaActualizada.getCuentaId())
                .montoRetirado(request.getMonto())
                .nuevoSaldo(cuentaActualizada.getSaldo())
                .estado("EXITOSO")
                .timestamp(LocalDateTime.now())
                .build();
    }
}