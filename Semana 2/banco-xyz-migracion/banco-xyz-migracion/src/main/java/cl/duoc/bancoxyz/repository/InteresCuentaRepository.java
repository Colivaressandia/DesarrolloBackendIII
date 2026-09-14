package cl.duoc.bancoxyz.repository;

import cl.duoc.bancoxyz.model.InteresCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para persistir los cálculos de intereses en 'intereses_calculados'.
 */
@Repository
public interface InteresCuentaRepository extends JpaRepository<InteresCuenta, Long> {
}