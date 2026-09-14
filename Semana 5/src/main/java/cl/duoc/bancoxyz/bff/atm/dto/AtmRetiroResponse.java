package cl.duoc.bancoxyz.bff.atm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AtmRetiroResponse {
    private String codigoAutorizacion;
    private Long cuentaId;
    private BigDecimal montoRetirado;
    private BigDecimal nuevoSaldo;
    private String estado;
    private LocalDateTime timestamp;

    public AtmRetiroResponse() {}

    public AtmRetiroResponse(String codigoAutorizacion, Long cuentaId, BigDecimal montoRetirado, BigDecimal nuevoSaldo, String estado, LocalDateTime timestamp) {
        this.codigoAutorizacion = codigoAutorizacion;
        this.cuentaId = cuentaId;
        this.montoRetirado = montoRetirado;
        this.nuevoSaldo = nuevoSaldo;
        this.estado = estado;
        this.timestamp = timestamp;
    }

    public static AtmRetiroResponseBuilder builder() { return new AtmRetiroResponseBuilder(); }

    public static class AtmRetiroResponseBuilder {
        private String codigoAutorizacion;
        private Long cuentaId;
        private BigDecimal montoRetirado;
        private BigDecimal nuevoSaldo;
        private String estado;
        private LocalDateTime timestamp;

        public AtmRetiroResponseBuilder codigoAutorizacion(String codigoAutorizacion) { this.codigoAutorizacion = codigoAutorizacion; return this; }
        public AtmRetiroResponseBuilder cuentaId(Long cuentaId) { this.cuentaId = cuentaId; return this; }
        public AtmRetiroResponseBuilder montoRetirado(BigDecimal montoRetirado) { this.montoRetirado = montoRetirado; return this; }
        public AtmRetiroResponseBuilder nuevoSaldo(BigDecimal nuevoSaldo) { this.nuevoSaldo = nuevoSaldo; return this; }
        public AtmRetiroResponseBuilder estado(String estado) { this.estado = estado; return this; }
        public AtmRetiroResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public AtmRetiroResponse build() { return new AtmRetiroResponse(codigoAutorizacion, cuentaId, montoRetirado, nuevoSaldo, estado, timestamp); }
    }

    public String getCodigoAutorizacion() { return codigoAutorizacion; }
    public void setCodigoAutorizacion(String codigoAutorizacion) { this.codigoAutorizacion = codigoAutorizacion; }
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public BigDecimal getMontoRetirado() { return montoRetirado; }
    public void setMontoRetirado(BigDecimal montoRetirado) { this.montoRetirado = montoRetirado; }
    public BigDecimal getNuevoSaldo() { return nuevoSaldo; }
    public void setNuevoSaldo(BigDecimal nuevoSaldo) { this.nuevoSaldo = nuevoSaldo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}