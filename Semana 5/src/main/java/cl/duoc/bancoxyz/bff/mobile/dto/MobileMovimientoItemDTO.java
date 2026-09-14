package cl.duoc.bancoxyz.bff.mobile.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MobileMovimientoItemDTO {
    private LocalDate fecha;
    private String tipo;
    private BigDecimal monto;

    public MobileMovimientoItemDTO() {}

    public MobileMovimientoItemDTO(LocalDate fecha, String tipo, BigDecimal monto) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.monto = monto;
    }

    public static MobileMovimientoItemDTOBuilder builder() { return new MobileMovimientoItemDTOBuilder(); }

    public static class MobileMovimientoItemDTOBuilder {
        private LocalDate fecha;
        private String tipo;
        private BigDecimal monto;

        public MobileMovimientoItemDTOBuilder fecha(LocalDate fecha) { this.fecha = fecha; return this; }
        public MobileMovimientoItemDTOBuilder tipo(String tipo) { this.tipo = tipo; return this; }
        public MobileMovimientoItemDTOBuilder monto(BigDecimal monto) { this.monto = monto; return this; }
        public MobileMovimientoItemDTO build() { return new MobileMovimientoItemDTO(fecha, tipo, monto); }
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}