package cl.duoc.bancoxyz.legacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "legacy_transacciones_log")
public class TransaccionLegacyEntity {

    @Id
    @Column(name = "registro_id")
    private Long id;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "fecha_raw")
    private String fechaRaw;

    @Column(name = "monto", precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "estado_registro")
    private String estadoRegistro;

    public TransaccionLegacyEntity() {}

    public TransaccionLegacyEntity(Long id, LocalDate fecha, String fechaRaw, BigDecimal monto, String tipo, String estadoRegistro) {
        this.id = id;
        this.fecha = fecha;
        this.fechaRaw = fechaRaw;
        this.monto = monto;
        this.tipo = tipo;
        this.estadoRegistro = estadoRegistro;
    }

    public static TransaccionLegacyEntityBuilder builder() {
        return new TransaccionLegacyEntityBuilder();
    }

    public static class TransaccionLegacyEntityBuilder {
        private Long id;
        private LocalDate fecha;
        private String fechaRaw;
        private BigDecimal monto;
        private String tipo;
        private String estadoRegistro;

        public TransaccionLegacyEntityBuilder id(Long id) { this.id = id; return this; }
        public TransaccionLegacyEntityBuilder fecha(LocalDate fecha) { this.fecha = fecha; return this; }
        public TransaccionLegacyEntityBuilder fechaRaw(String fechaRaw) { this.fechaRaw = fechaRaw; return this; }
        public TransaccionLegacyEntityBuilder monto(BigDecimal monto) { this.monto = monto; return this; }
        public TransaccionLegacyEntityBuilder tipo(String tipo) { this.tipo = tipo; return this; }
        public TransaccionLegacyEntityBuilder estadoRegistro(String estadoRegistro) { this.estadoRegistro = estadoRegistro; return this; }
        public TransaccionLegacyEntity build() { return new TransaccionLegacyEntity(id, fecha, fechaRaw, monto, tipo, estadoRegistro); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getFechaRaw() { return fechaRaw; }
    public void setFechaRaw(String fechaRaw) { this.fechaRaw = fechaRaw; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstadoRegistro() { return estadoRegistro; }
    public void setEstadoRegistro(String estadoRegistro) { this.estadoRegistro = estadoRegistro; }
}