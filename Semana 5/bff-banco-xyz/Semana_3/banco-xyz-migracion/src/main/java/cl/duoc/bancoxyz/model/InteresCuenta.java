package cl.duoc.bancoxyz.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa la tabla 'intereses_calculados' en MySQL.
 * Persiste los resultados del cálculo de intereses mensuales aplicados según el tipo de cuenta.
 */
@Entity
@Table(name = "intereses_calculados")
public class InteresCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cuentaId;
    private String nombre;
    private Double saldoInicial;
    private Integer edad;
    private String tipo; // ahorro, prestamo, hipoteca
    private Double tasaAplicada; // Porcentaje aplicado en formato decimal (ej: 0.05 para 5%)
    private Double interesCalculado; // Monto monetario del interés generado
    private Double saldoFinal; // Saldo inicial + interés calculado

    /**
     * Constructor por defecto requerido por JPA.
     */
    public InteresCuenta() {}

    /**
     * Constructor con todos los campos calculados listo para inserción en base de datos.
     */
    public InteresCuenta(Long cuentaId, String nombre, Double saldoInicial, Integer edad, 
                         String tipo, Double tasaAplicada, Double interesCalculado, Double saldoFinal) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldoInicial = saldoInicial;
        this.edad = edad;
        this.tipo = tipo;
        this.tasaAplicada = tasaAplicada;
        this.interesCalculado = interesCalculado;
        this.saldoFinal = saldoFinal;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(Double saldoInicial) { this.saldoInicial = saldoInicial; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getTasaAplicada() { return tasaAplicada; }
    public void setTasaAplicada(Double tasaAplicada) { this.tasaAplicada = tasaAplicada; }

    public Double getInteresCalculado() { return interesCalculado; }
    public void setInteresCalculado(Double interesCalculado) { this.interesCalculado = interesCalculado; }

    public Double getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(Double saldoFinal) { this.saldoFinal = saldoFinal; }
}