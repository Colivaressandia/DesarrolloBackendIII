package com.bancoxyz.pagos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "eventos_outbox", indexes = {
        @Index(name = "idx_outbox_pendiente", columnList = "publicadoEn, creadoEn")
})
public class EventoOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String eventoId;

    @Column(nullable = false, length = 36)
    private String pagoId;

    @Column(nullable = false)
    private Long cuentaOrigen;

    @Column(nullable = false)
    private Long cuentaDestino;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(nullable = false)
    private Instant creadoEn;

    private Instant publicadoEn;

    @Column(nullable = false)
    private int intentos;

    protected EventoOutbox() {
    }

    public EventoOutbox(String eventoId, Pago pago) {
        this.eventoId = eventoId;
        this.pagoId = pago.getId();
        this.cuentaOrigen = pago.getCuentaOrigen();
        this.cuentaDestino = pago.getCuentaDestino();
        this.monto = pago.getMonto();
        this.moneda = pago.getMoneda();
        this.creadoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getPagoId() {
        return pagoId;
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

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getPublicadoEn() {
        return publicadoEn;
    }

    public int getIntentos() {
        return intentos;
    }

    public void marcarPublicado() {
        publicadoEn = Instant.now();
    }

    public void registrarIntentoFallido() {
        intentos++;
    }
}
