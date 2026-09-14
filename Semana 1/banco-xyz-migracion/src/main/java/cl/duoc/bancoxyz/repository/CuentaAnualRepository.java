package cl.duoc.bancoxyz.repository;

import cl.duoc.bancoxyz.model.CuentaAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para persistir los estados de cuenta auditados en 'estados_cuenta_anual'.
 */
@Repository
public interface CuentaAnualRepository extends JpaRepository<CuentaAnual, Long> {
}