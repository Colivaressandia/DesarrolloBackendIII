package cl.duoc.bancoxyz.dto;

/**
 * Data Transfer Object (DTO) utilizado por Spring Batch para mapear
 * directamente las columnas leídas desde 'data/transacciones.csv'.
 */
public class TransaccionDTO {

    private Long id;
    private String fecha;
    private Double monto;
    private String tipo;

    // Getters y Setters necesarios para BeanWrapperFieldSetMapper
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}