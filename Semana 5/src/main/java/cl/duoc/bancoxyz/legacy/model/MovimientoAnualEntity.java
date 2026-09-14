package cl.duoc.bancoxyz.legacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "legacy_movimientos_anuales")
public class MovimientoAnualEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "fecha_raw")
    private String fechaRaw;

    @Column(name = "transaccion")
    private String transaccion;

    @Column(name = "monto", precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "descripcion")
    private String descripcion;

    public MovimientoAnualEntity() {}

    public MovimientoAnualEntity(Long id, Long cuentaId, LocalDate fecha, String fechaRaw, String transaccion, BigDecimal monto, String descripcion) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.fechaRaw = fechaRaw;
        this.transaccion = transaccion;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public static MovimientoAnualEntityBuilder builder() {
        return new MovimientoAnualEntityBuilder();
    }

    public static class MovimientoAnualEntityBuilder {
        private Long id;
        private Long cuentaId;
        private LocalDate fecha;
        private String fechaRaw;
        private String transaccion;
        private BigDecimal monto;
        private String descripcion;

        public MovimientoAnualEntityBuilder id(Long id) { this.id = id; return this; }
        public MovimientoAnualEntityBuilder cuentaId(Long cuentaId) { this.cuentaId = cuentaId; return this; }
        public MovimientoAnualEntityBuilder fecha(LocalDate fecha) { this.fecha = fecha; return this; }
        public MovimientoAnualEntityBuilder fechaRaw(String fechaRaw) { this.fechaRaw = fechaRaw; return this; }
        public MovimientoAnualEntityBuilder transaccion(String transaccion) { this.transaccion = transaccion; return this; }
        public MovimientoAnualEntityBuilder monto(BigDecimal monto) { this.monto = monto; return this; }
        public MovimientoAnualEntityBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public MovimientoAnualEntity build() { return new MovimientoAnualEntity(id, cuentaId, fecha, fechaRaw, transaccion, monto, descripcion); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getFechaRaw() { return fechaRaw; }
    public void setFechaRaw(String fechaRaw) { this.fechaRaw = fechaRaw; }
    public String getTransaccion() { return transaccion; }
    public void setTransaccion(String transaccion) { this.transaccion = transaccion; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}