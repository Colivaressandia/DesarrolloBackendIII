package cl.duoc.bancoxyz.bff.web.service;

import cl.duoc.bancoxyz.bff.web.dto.WebCuentaDetalleDTO;
import cl.duoc.bancoxyz.bff.web.dto.WebMovimientoDTO;
import cl.duoc.bancoxyz.bff.web.dto.WebPosicionGlobalResponse;
import cl.duoc.bancoxyz.legacy.model.CuentaEntity;
import cl.duoc.bancoxyz.legacy.model.MovimientoAnualEntity;
import cl.duoc.bancoxyz.legacy.service.LegacyCoreService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BffWebService {

    private final LegacyCoreService coreService;

    public BffWebService(LegacyCoreService coreService) {
        this.coreService = coreService;
    }

    public WebPosicionGlobalResponse obtenerPosicionGlobal(String nombreCliente) {
        List<CuentaEntity> cuentas = coreService.obtenerCuentasPorCliente(nombreCliente);
        if (cuentas.isEmpty()) {
            throw new IllegalArgumentException("Cliente no encontrado: " + nombreCliente);
        }

        BigDecimal patrimonioTotal = BigDecimal.ZERO;
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;
        List<WebCuentaDetalleDTO> cuentasDTO = new ArrayList<>();
        Integer edad = null;

        for (CuentaEntity cuenta : cuentas) {
            if (edad == null && cuenta.getEdad() != null) {
                edad = cuenta.getEdad();
            }
            patrimonioTotal = patrimonioTotal.add(cuenta.getSaldo() != null ? cuenta.getSaldo() : BigDecimal.ZERO);

            List<MovimientoAnualEntity> movimientos = coreService.obtenerMovimientosPorCuenta(cuenta.getCuentaId());
            List<WebMovimientoDTO> movimientosDTO = new ArrayList<>();

            for (MovimientoAnualEntity mov : movimientos) {
                BigDecimal monto = mov.getMonto() != null ? mov.getMonto() : BigDecimal.ZERO;
                if ("deposito".equalsIgnoreCase(mov.getTransaccion())) {
                    totalIngresos = totalIngresos.add(monto.abs());
                } else {
                    totalEgresos = totalEgresos.add(monto.abs());
                }

                movimientosDTO.add(WebMovimientoDTO.builder()
                        .id(mov.getId())
                        .fecha(mov.getFecha())
                        .fechaOriginal(mov.getFechaRaw())
                        .tipoOperacion(mov.getTransaccion())
                        .monto(monto)
                        .descripcion(mov.getDescripcion())
                        .estado(mov.getFecha() == null ? "FECHA_INVALIDA" : "OK")
                        .build());
            }

            cuentasDTO.add(WebCuentaDetalleDTO.builder()
                    .cuentaId(cuenta.getCuentaId())
                    .tipoProducto(cuenta.getTipo())
                    .saldoActual(cuenta.getSaldo())
                    .estadoValido(cuenta.getEsValida())
                    .totalMovimientos(movimientos.size())
                    .movimientos(movimientosDTO)
                    .build());
        }

        return WebPosicionGlobalResponse.builder()
                .cliente(nombreCliente)
                .edad(edad)
                .patrimonioTotal(patrimonioTotal)
                .totalProductos(cuentas.size())
                .totalIngresos(totalIngresos)
                .totalEgresos(totalEgresos)
                .cuentas(cuentasDTO)
                .build();
    }
}