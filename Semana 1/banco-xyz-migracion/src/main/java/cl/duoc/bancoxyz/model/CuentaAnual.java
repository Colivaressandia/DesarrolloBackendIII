package cl.duoc.bancoxyz.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Entidad JPA que representa la tabla 'estados_cuenta_anual' en MySQL.
 * Compila y categoriza las operaciones financieras para el informe anual de auditoría.
 */
@Entity
@Table(name = "estados_cuenta_anual")
public class CuentaAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion; // deposito, retiro, compra
    private Double monto;
    private String descripcion;
    private String clasificacionAuditoria; // INGRESO, EGRESO o MONTO_NULO

    /**
     * Constructor por defecto para JPA.
     */
    public CuentaAnual() {}

    /**
     * Constructor completo con auditoría procesada.
     */
    public CuentaAnual(Long cuentaId, LocalDate fecha, String transaccion, 
                       Double monto, String descripcion, String clasificacionAuditoria) {
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.transaccion = transaccion;
        this.monto = monto;
        this.descripcion = descripcion;
        this.clasificacionAuditoria = clasificacionAuditoria;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getTransaccion() { return transaccion; }
    public void setTransaccion(String transaccion) { this.transaccion = transaccion; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getClasificacionAuditoria() { return clasificacionAuditoria; }
    public void setClasificacionAuditoria(String clasificacionAuditoria) { this.clasificacionAuditoria = clasificacionAuditoria; }
}