package cl.duoc.bancoxyz.legacy.repository;

import cl.duoc.bancoxyz.legacy.model.MovimientoAnualEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoAnualRepository extends JpaRepository<MovimientoAnualEntity, Long> {
    List<MovimientoAnualEntity> findByCuentaIdOrderByFechaDesc(Long cuentaId);
}