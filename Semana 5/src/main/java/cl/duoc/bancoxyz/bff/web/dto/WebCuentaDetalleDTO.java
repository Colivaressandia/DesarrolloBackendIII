package cl.duoc.bancoxyz.bff.web.dto;

import java.math.BigDecimal;
import java.util.List;

public class WebCuentaDetalleDTO {
    private Long cuentaId;
    private String tipoProducto;
    private BigDecimal saldoActual;
    private Boolean estadoValido;
    private Integer totalMovimientos;
    private List<WebMovimientoDTO> movimientos;

    public WebCuentaDetalleDTO() {}

    public WebCuentaDetalleDTO(Long cuentaId, String tipoProducto, BigDecimal saldoActual, Boolean estadoValido, Integer totalMovimientos, List<WebMovimientoDTO> movimientos) {
        this.cuentaId = cuentaId;
        this.tipoProducto = tipoProducto;
        this.saldoActual = saldoActual;
        this.estadoValido = estadoValido;
        this.totalMovimientos = totalMovimientos;
        this.movimientos = movimientos;
    }

    public static WebCuentaDetalleDTOBuilder builder() { return new WebCuentaDetalleDTOBuilder(); }

    public static class WebCuentaDetalleDTOBuilder {
        private Long cuentaId;
        private String tipoProducto;
        private BigDecimal saldoActual;
        private Boolean estadoValido;
        private Integer totalMovimientos;
        private List<WebMovimientoDTO> movimientos;

        public WebCuentaDetalleDTOBuilder cuentaId(Long cuentaId) { this.cuentaId = cuentaId; return this; }
        public WebCuentaDetalleDTOBuilder tipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; return this; }
        public WebCuentaDetalleDTOBuilder saldoActual(BigDecimal saldoActual) { this.saldoActual = saldoActual; return this; }
        public WebCuentaDetalleDTOBuilder estadoValido(Boolean estadoValido) { this.estadoValido = estadoValido; return this; }
        public WebCuentaDetalleDTOBuilder totalMovimientos(Integer totalMovimientos) { this.totalMovimientos = totalMovimientos; return this; }
        public WebCuentaDetalleDTOBuilder movimientos(List<WebMovimientoDTO> movimientos) { this.movimientos = movimientos; return this; }
        public WebCuentaDetalleDTO build() { return new WebCuentaDetalleDTO(cuentaId, tipoProducto, saldoActual, estadoValido, totalMovimientos, movimientos); }
    }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }
    public BigDecimal getSaldoActual() { return saldoActual; }
    public void setSaldoActual(BigDecimal saldoActual) { this.saldoActual = saldoActual; }
    public Boolean getEstadoValido() { return estadoValido; }
    public void setEstadoValido(Boolean estadoValido) { this.estadoValido = estadoValido; }
    public Integer getTotalMovimientos() { return totalMovimientos; }
    public void setTotalMovimientos(Integer totalMovimientos) { this.totalMovimientos = totalMovimientos; }
    public List<WebMovimientoDTO> getMovimientos() { return movimientos; }
    public void setMovimientos(List<WebMovimientoDTO> movimientos) { this.movimientos = movimientos; }
}