package com.bancoxyz.banco_microservicio.event;

public class TransaccionCreadaEvent {

    private Long transaccionId;
    private String fecha;
    private Double monto;
    private String tipo;

    public TransaccionCreadaEvent() {
    }

    public TransaccionCreadaEvent(Long transaccionId, String fecha, Double monto, String tipo) {
        this.transaccionId = transaccionId;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
    }

    public Long getTransaccionId() {
        return transaccionId;
    }

    public void setTransaccionId(Long transaccionId) {
        this.transaccionId = transaccionId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}