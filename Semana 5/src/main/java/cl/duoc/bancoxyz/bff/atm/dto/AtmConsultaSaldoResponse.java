package cl.duoc.bancoxyz.bff.atm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AtmConsultaSaldoResponse {
    private Long cuentaId;
    private BigDecimal saldoDisponible;
    private String moneda;
    private LocalDateTime timestampOperacion;

    public AtmConsultaSaldoResponse() {}

    public AtmConsultaSaldoResponse(Long cuentaId, BigDecimal saldoDisponible, String moneda, LocalDateTime timestampOperacion) {
        this.cuentaId = cuentaId;
        this.saldoDisponible = saldoDisponible;
        this.moneda = moneda;
        this.timestampOperacion = timestampOperacion;
    }

    public static AtmConsultaSaldoResponseBuilder builder() { return new AtmConsultaSaldoResponseBuilder(); }

    public static class AtmConsultaSaldoResponseBuilder {
        private Long cuentaId;
        private BigDecimal saldoDisponible;
        private String moneda;
        private LocalDateTime timestampOperacion;

        public AtmConsultaSaldoResponseBuilder cuentaId(Long cuentaId) { this.cuentaId = cuentaId; return this; }
        public AtmConsultaSaldoResponseBuilder saldoDisponible(BigDecimal saldoDisponible) { this.saldoDisponible = saldoDisponible; return this; }
        public AtmConsultaSaldoResponseBuilder moneda(String moneda) { this.moneda = moneda; return this; }
        public AtmConsultaSaldoResponseBuilder timestampOperacion(LocalDateTime timestampOperacion) { this.timestampOperacion = timestampOperacion; return this; }
        public AtmConsultaSaldoResponse build() { return new AtmConsultaSaldoResponse(cuentaId, saldoDisponible, moneda, timestampOperacion); }
    }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public BigDecimal getSaldoDisponible() { return saldoDisponible; }
    public void setSaldoDisponible(BigDecimal saldoDisponible) { this.saldoDisponible = saldoDisponible; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public LocalDateTime getTimestampOperacion() { return timestampOperacion; }
    public void setTimestampOperacion(LocalDateTime timestampOperacion) { this.timestampOperacion = timestampOperacion; }
}