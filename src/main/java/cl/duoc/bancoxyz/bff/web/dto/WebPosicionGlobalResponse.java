package cl.duoc.bancoxyz.bff.web.dto;

import java.math.BigDecimal;
import java.util.List;

public class WebPosicionGlobalResponse {
    private String cliente;
    private Integer edad;
    private BigDecimal patrimonioTotal;
    private Integer totalProductos;
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private List<WebCuentaDetalleDTO> cuentas;

    public WebPosicionGlobalResponse() {}

    public WebPosicionGlobalResponse(String cliente, Integer edad, BigDecimal patrimonioTotal, Integer totalProductos, BigDecimal totalIngresos, BigDecimal totalEgresos, List<WebCuentaDetalleDTO> cuentas) {
        this.cliente = cliente;
        this.edad = edad;
        this.patrimonioTotal = patrimonioTotal;
        this.totalProductos = totalProductos;
        this.totalIngresos = totalIngresos;
        this.totalEgresos = totalEgresos;
        this.cuentas = cuentas;
    }

    public static WebPosicionGlobalResponseBuilder builder() { return new WebPosicionGlobalResponseBuilder(); }

    public static class WebPosicionGlobalResponseBuilder {
        private String cliente;
        private Integer edad;
        private BigDecimal patrimonioTotal;
        private Integer totalProductos;
        private BigDecimal totalIngresos;
        private BigDecimal totalEgresos;
        private List<WebCuentaDetalleDTO> cuentas;

        public WebPosicionGlobalResponseBuilder cliente(String cliente) { this.cliente = cliente; return this; }
        public WebPosicionGlobalResponseBuilder edad(Integer edad) { this.edad = edad; return this; }
        public WebPosicionGlobalResponseBuilder patrimonioTotal(BigDecimal patrimonioTotal) { this.patrimonioTotal = patrimonioTotal; return this; }
        public WebPosicionGlobalResponseBuilder totalProductos(Integer totalProductos) { this.totalProductos = totalProductos; return this; }
        public WebPosicionGlobalResponseBuilder totalIngresos(BigDecimal totalIngresos) { this.totalIngresos = totalIngresos; return this; }
        public WebPosicionGlobalResponseBuilder totalEgresos(BigDecimal totalEgresos) { this.totalEgresos = totalEgresos; return this; }
        public WebPosicionGlobalResponseBuilder cuentas(List<WebCuentaDetalleDTO> cuentas) { this.cuentas = cuentas; return this; }
        public WebPosicionGlobalResponse build() { return new WebPosicionGlobalResponse(cliente, edad, patrimonioTotal, totalProductos, totalIngresos, totalEgresos, cuentas); }
    }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public BigDecimal getPatrimonioTotal() { return patrimonioTotal; }
    public void setPatrimonioTotal(BigDecimal patrimonioTotal) { this.patrimonioTotal = patrimonioTotal; }
    public Integer getTotalProductos() { return totalProductos; }
    public void setTotalProductos(Integer totalProductos) { this.totalProductos = totalProductos; }
    public BigDecimal getTotalIngresos() { return totalIngresos; }
    public void setTotalIngresos(BigDecimal totalIngresos) { this.totalIngresos = totalIngresos; }
    public BigDecimal getTotalEgresos() { return totalEgresos; }
    public void setTotalEgresos(BigDecimal totalEgresos) { this.totalEgresos = totalEgresos; }
    public List<WebCuentaDetalleDTO> getCuentas() { return cuentas; }
    public void setCuentas(List<WebCuentaDetalleDTO> cuentas) { this.cuentas = cuentas; }
}