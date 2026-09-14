package cl.duoc.bancoxyz.bff.atm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class AtmRetiroRequest {
    @NotNull(message = "El ID de cuenta es obligatorio")
    private Long cuentaId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    private String terminalId;

    public AtmRetiroRequest() {}

    public AtmRetiroRequest(Long cuentaId, BigDecimal monto, String terminalId) {
        this.cuentaId = cuentaId;
        this.monto = monto;
        this.terminalId = terminalId;
    }

    public static AtmRetiroRequestBuilder builder() { return new AtmRetiroRequestBuilder(); }

    public static class AtmRetiroRequestBuilder {
        private Long cuentaId;
        private BigDecimal monto;
        private String terminalId;

        public AtmRetiroRequestBuilder cuentaId(Long cuentaId) { this.cuentaId = cuentaId; return this; }
        public AtmRetiroRequestBuilder monto(BigDecimal monto) { this.monto = monto; return this; }
        public AtmRetiroRequestBuilder terminalId(String terminalId) { this.terminalId = terminalId; return this; }
        public AtmRetiroRequest build() { return new AtmRetiroRequest(cuentaId, monto, terminalId); }
    }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
}