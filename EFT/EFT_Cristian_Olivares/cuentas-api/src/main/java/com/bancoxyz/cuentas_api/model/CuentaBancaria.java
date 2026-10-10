package com.bancoxyz.cuentas_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas_bancarias")
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCuenta tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    protected CuentaBancaria() {
    }

    public CuentaBancaria(Long clienteId, TipoCuenta tipo, BigDecimal saldo) {
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.saldo = saldo;
        this.estado = EstadoCuenta.ACTIVA;
        this.fechaApertura = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public TipoCuenta getTipo() {
        return tipo;
    }

    public void setTipo(TipoCuenta tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void cerrar() {
        estado = EstadoCuenta.CERRADA;
    }

    public void debitar(BigDecimal monto) {
        saldo = saldo.subtract(monto);
    }

    public void abonar(BigDecimal monto) {
        saldo = saldo.add(monto);
    }

    public enum TipoCuenta {
        AHORRO,
        CORRIENTE
    }

    public enum EstadoCuenta {
        ACTIVA,
        CERRADA
    }
}
