package com.bancoxyz.pagos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private Long cuentaOrigen;

    @Column(nullable = false)
    private Long cuentaDestino;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(nullable = false, unique = true, length = 80)
    private String claveIdempotencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(nullable = false)
    private Instant creadoEn;

    protected Pago() {
    }

    public Pago(String id, Long cuentaOrigen, Long cuentaDestino, BigDecimal monto,
                String moneda, String claveIdempotencia) {
        this.id = id;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.moneda = moneda;
        this.claveIdempotencia = claveIdempotencia;
        this.estado = EstadoPago.ACEPTADO;
        this.creadoEn = Instant.now();
    }

    public String getId() {
        return id;
    }

    public Long getCuentaOrigen() {
        return cuentaOrigen;
    }

    public Long getCuentaDestino() {
        return cuentaDestino;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void marcarProcesado() {
        estado = EstadoPago.PROCESADO;
    }

    public enum EstadoPago {
        ACEPTADO,
        PROCESADO
    }
}
