package cl.duoc.bancoxyz.repository;

import cl.duoc.bancoxyz.model.ResumenCuentaAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la persistencia del resumen anual consolidado de cuentas.
 * 
 * @author Duoc UC - Desarrollo Backend III
 * @version 3.0
 */
@Repository
public interface ResumenCuentaAnualRepository extends JpaRepository<ResumenCuentaAnual, Long> {
}