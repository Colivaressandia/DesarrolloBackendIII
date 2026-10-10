package com.bancoxyz.cuentas_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transferencias_procesadas")
public class TransferenciaProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String claveIdempotencia;

    @Column(nullable = false)
    private Long cuentaOrigen;

    @Column(nullable = false)
    private Long cuentaDestino;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDateTime procesadaEn;

    protected TransferenciaProcesada() {
    }

    public TransferenciaProcesada(String claveIdempotencia, Long cuentaOrigen,
                                  Long cuentaDestino, BigDecimal monto) {
        this.claveIdempotencia = claveIdempotencia;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.procesadaEn = LocalDateTime.now();
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
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
}
