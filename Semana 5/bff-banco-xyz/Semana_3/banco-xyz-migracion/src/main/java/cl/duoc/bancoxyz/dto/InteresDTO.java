package cl.duoc.bancoxyz.dto;

/**
 * DTO para mapear las columnas del archivo 'data/intereses.csv'.
 */
public class InteresDTO {

    private Long cuentaId;
    private String nombre;
    private Double saldo;
    private Integer edad;
    private String tipo;

    // Getters y Setters
    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}