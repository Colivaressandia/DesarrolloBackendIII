package cl.duoc.bancoxyz.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidad JPA para almacenar el estado consolidado anual por cuenta bancaria.
 * Generado durante el Step de agregacion contable para fines de auditoria.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
@Entity
@Table(name = "resumen_anual_cuentas")
public class ResumenCuentaAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id", nullable = false)
    private String cuentaId;

    @Column(name = "total_ingresos", nullable = false)
    private BigDecimal totalIngresos;

    @Column(name = "total_egresos", nullable = false)
    private BigDecimal totalEgresos;

    @Column(name = "balance_neto", nullable = false)
    private BigDecimal balanceNeto;

    @Column(name = "cantidad_movimientos", nullable = false)
    private Integer cantidadMovimientos;

    @Column(name = "estado_auditoria", nullable = false)
    private String estadoAuditoria;

    public ResumenCuentaAnual() {
    }

    public ResumenCuentaAnual(String cuentaId, BigDecimal totalIngresos, BigDecimal totalEgresos, 
                              BigDecimal balanceNeto, Integer cantidadMovimientos, String estadoAuditoria) {
        this.cuentaId = cuentaId;
        this.totalIngresos = totalIngresos;
        this.totalEgresos = totalEgresos;
        this.balanceNeto = balanceNeto;
        this.cantidadMovimientos = cantidadMovimientos;
        this.estadoAuditoria = estadoAuditoria;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(String cuentaId) {
        this.cuentaId = cuentaId;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public BigDecimal getTotalEgresos() {
        return totalEgresos;
    }

    public void setTotalEgresos(BigDecimal totalEgresos) {
        this.totalEgresos = totalEgresos;
    }

    public BigDecimal getBalanceNeto() {
        return balanceNeto;
    }

    public void setBalanceNeto(BigDecimal balanceNeto) {
        this.balanceNeto = balanceNeto;
    }

    public Integer getCantidadMovimientos() {
        return cantidadMovimientos;
    }

    public void setCantidadMovimientos(Integer cantidadMovimientos) {
        this.cantidadMovimientos = cantidadMovimientos;
    }

    public String getEstadoAuditoria() {
        return estadoAuditoria;
    }

    public void setEstadoAuditoria(String estadoAuditoria) {
        this.estadoAuditoria = estadoAuditoria;
    }
}