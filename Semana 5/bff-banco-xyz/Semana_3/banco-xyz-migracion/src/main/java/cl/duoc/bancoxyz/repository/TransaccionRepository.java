package cl.duoc.bancoxyz.repository;

import cl.duoc.bancoxyz.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para operaciones CRUD sobre la tabla 'reporte_transacciones'.
 * Es utilizado por el RepositoryItemWriter de Spring Batch para guardar los registros procesados.
 */
@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
}