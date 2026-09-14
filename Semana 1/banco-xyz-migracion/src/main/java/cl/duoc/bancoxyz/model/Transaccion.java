package cl.duoc.bancoxyz.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Entidad JPA que representa la tabla 'reporte_transacciones' en la base de datos MySQL.
 * Almacena las transacciones diarias procesadas junto con su estado de auditoría (válida o anomalía).
 */
@Entity
@Table(name = "reporte_transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador autonumérico interno de la base de datos

    private Long originalId; // ID que viene originalmente desde el archivo CSV
    private LocalDate fecha;
    private Double monto;
    private String tipo; // Tipo de transacción: débito, crédito, etc.
    private String estado; // Clasificación generada por el Processor (ej. VALIDA, ANOMALIA_MONTO_NEGATIVO)

    /**
     * Constructor por defecto requerido obligatoriamente por JPA / Hibernate.
     */
    public Transaccion() {}

    /**
     * Constructor completo para instanciar la entidad luego del procesamiento batch.
     */
    public Transaccion(Long originalId, LocalDate fecha, Double monto, String tipo, String estado) {
        this.originalId = originalId;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOriginalId() { return originalId; }
    public void setOriginalId(Long originalId) { this.originalId = originalId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}