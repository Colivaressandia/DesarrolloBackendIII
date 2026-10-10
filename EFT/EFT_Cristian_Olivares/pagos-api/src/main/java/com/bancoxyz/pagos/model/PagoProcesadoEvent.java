package com.bancoxyz.pagos.model;

import java.math.BigDecimal;
import java.time.Instant;

public record PagoProcesadoEvent(String eventoId, String pagoId, Long cuentaOrigen,
                                 Long cuentaDestino, BigDecimal monto, String moneda,
                                 Instant creadoEn) {

    public static PagoProcesadoEvent from(EventoOutbox event) {
        return new PagoProcesadoEvent(event.getEventoId(), event.getPagoId(),
                event.getCuentaOrigen(), event.getCuentaDestino(), event.getMonto(),
                event.getMoneda(), event.getCreadoEn());
    }
}
