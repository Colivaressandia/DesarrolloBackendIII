package cl.duoc.bancoxyz.bff.mobile.service;

import cl.duoc.bancoxyz.bff.mobile.dto.MobileMovimientoItemDTO;
import cl.duoc.bancoxyz.bff.mobile.dto.MobileResumenCuentaResponse;
import cl.duoc.bancoxyz.legacy.model.CuentaEntity;
import cl.duoc.bancoxyz.legacy.model.MovimientoAnualEntity;
import cl.duoc.bancoxyz.legacy.service.LegacyCoreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BffMobileService {

    private final LegacyCoreService coreService;

    public BffMobileService(LegacyCoreService coreService) {
        this.coreService = coreService;
    }

    public MobileResumenCuentaResponse obtenerResumen(Long cuentaId) {
        CuentaEntity cuenta = coreService.obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + cuentaId));

        List<MovimientoAnualEntity> movimientos = coreService.obtenerMovimientosPorCuenta(cuentaId);

        List<MobileMovimientoItemDTO> ultimos = movimientos.stream()
                .limit(5)
                .map(m -> MobileMovimientoItemDTO.builder()
                        .fecha(m.getFecha())
                        .tipo(m.getTransaccion())
                        .monto(m.getMonto())
                        .build())
                .collect(Collectors.toList());

        return MobileResumenCuentaResponse.builder()
                .cuentaId(cuenta.getCuentaId())
                .titular(cuenta.getNombre())
                .tipo(cuenta.getTipo())
                .saldoDisponible(cuenta.getSaldo())
                .ultimosMovimientos(ultimos)
                .build();
    }
}