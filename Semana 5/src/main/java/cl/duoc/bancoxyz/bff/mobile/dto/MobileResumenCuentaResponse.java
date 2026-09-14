package cl.duoc.bancoxyz.bff.mobile.dto;

import java.math.BigDecimal;
import java.util.List;

public class MobileResumenCuentaResponse {
    private Long cuentaId;
    private String titular;
    private String tipo;
    private BigDecimal saldoDisponible;
    private List<MobileMovimientoItemDTO> ultimosMovimientos;

    public MobileResumenCuentaResponse() {}

    public MobileResumenCuentaResponse(Long cuentaId, String titular, String tipo, BigDecimal saldoDisponible, List<MobileMovimientoItemDTO> ultimosMovimientos) {
        this.cuentaId = cuentaId;
        this.titular = titular;
        this.tipo = tipo;
        this.saldoDisponible = saldoDisponible;
        this.ultimosMovimientos = ultimosMovimientos;
    }

    public static MobileResumenCuentaResponseBuilder builder() { return new MobileResumenCuentaResponseBuilder(); }

    public static class MobileResumenCuentaResponseBuilder {
        private Long cuentaId;
        private String titular;
        private String tipo;
        private BigDecimal saldoDisponible;
        private List<MobileMovimientoItemDTO> ultimosMovimientos;

        public MobileResumenCuentaResponseBuilder cuentaId(Long cuentaId) { this.cuentaId = cuentaId; return this; }
        public MobileResumenCuentaResponseBuilder titular(String titular) { this.titular = titular; return this; }
        public MobileResumenCuentaResponseBuilder tipo(String tipo) { this.tipo = tipo; return this; }
        public MobileResumenCuentaResponseBuilder saldoDisponible(BigDecimal saldoDisponible) { this.saldoDisponible = saldoDisponible; return this; }
        public MobileResumenCuentaResponseBuilder ultimosMovimientos(List<MobileMovimientoItemDTO> ultimosMovimientos) { this.ultimosMovimientos = ultimosMovimientos; return this; }
        public MobileResumenCuentaResponse build() { return new MobileResumenCuentaResponse(cuentaId, titular, tipo, saldoDisponible, ultimosMovimientos); }
    }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getSaldoDisponible() { return saldoDisponible; }
    public void setSaldoDisponible(BigDecimal saldoDisponible) { this.saldoDisponible = saldoDisponible; }
    public List<MobileMovimientoItemDTO> getUltimosMovimientos() { return ultimosMovimientos; }
    public void setUltimosMovimientos(List<MobileMovimientoItemDTO> ultimosMovimientos) { this.ultimosMovimientos = ultimosMovimientos; }
}