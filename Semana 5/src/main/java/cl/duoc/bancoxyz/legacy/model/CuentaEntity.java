package cl.duoc.bancoxyz.legacy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "legacy_cuentas")
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "saldo", precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "es_valida")
    private Boolean esValida;

    public CuentaEntity() {}

    public CuentaEntity(Long id, Long cuentaId, String nombre, BigDecimal saldo, Integer edad, String tipo, Boolean esValida) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldo = saldo;
        this.edad = edad;
        this.tipo = tipo;
        this.esValida = esValida;
    }

    public static CuentaEntityBuilder builder() {
        return new CuentaEntityBuilder();
    }

    public static class CuentaEntityBuilder {
        private Long id;
        private Long cuentaId;
        private String nombre;
        private BigDecimal saldo;
        private Integer edad;
        private String tipo;
        private Boolean esValida;

        public CuentaEntityBuilder id(Long id) { this.id = id; return this; }
        public CuentaEntityBuilder cuentaId(Long cuentaId) { this.cuentaId = cuentaId; return this; }
        public CuentaEntityBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public CuentaEntityBuilder saldo(BigDecimal saldo) { this.saldo = saldo; return this; }
        public CuentaEntityBuilder edad(Integer edad) { this.edad = edad; return this; }
        public CuentaEntityBuilder tipo(String tipo) { this.tipo = tipo; return this; }
        public CuentaEntityBuilder esValida(Boolean esValida) { this.esValida = esValida; return this; }
        public CuentaEntity build() { return new CuentaEntity(id, cuentaId, nombre, saldo, edad, tipo, esValida); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Boolean getEsValida() { return esValida; }
    public void setEsValida(Boolean esValida) { this.esValida = esValida; }
}