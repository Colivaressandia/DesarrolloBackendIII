package cl.duoc.bancoxyz.bff.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WebMovimientoDTO {
    private Long id;
    private LocalDate fecha;
    private String fechaOriginal;
    private String tipoOperacion;
    private BigDecimal monto;
    private String descripcion;
    private String estado;

    public WebMovimientoDTO() {}

    public WebMovimientoDTO(Long id, LocalDate fecha, String fechaOriginal, String tipoOperacion, BigDecimal monto, String descripcion, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.fechaOriginal = fechaOriginal;
        this.tipoOperacion = tipoOperacion;
        this.monto = monto;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public static WebMovimientoDTOBuilder builder() { return new WebMovimientoDTOBuilder(); }

    public static class WebMovimientoDTOBuilder {
        private Long id;
        private LocalDate fecha;
        private String fechaOriginal;
        private String tipoOperacion;
        private BigDecimal monto;
        private String descripcion;
        private String estado;

        public WebMovimientoDTOBuilder id(Long id) { this.id = id; return this; }
        public WebMovimientoDTOBuilder fecha(LocalDate fecha) { this.fecha = fecha; return this; }
        public WebMovimientoDTOBuilder fechaOriginal(String fechaOriginal) { this.fechaOriginal = fechaOriginal; return this; }
        public WebMovimientoDTOBuilder tipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; return this; }
        public WebMovimientoDTOBuilder monto(BigDecimal monto) { this.monto = monto; return this; }
        public WebMovimientoDTOBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public WebMovimientoDTOBuilder estado(String estado) { this.estado = estado; return this; }
        public WebMovimientoDTO build() { return new WebMovimientoDTO(id, fecha, fechaOriginal, tipoOperacion, monto, descripcion, estado); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getFechaOriginal() { return fechaOriginal; }
    public void setFechaOriginal(String fechaOriginal) { this.fechaOriginal = fechaOriginal; }
    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}