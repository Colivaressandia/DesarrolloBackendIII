package cl.duoc.bancoxyz.legacy.repository;

import cl.duoc.bancoxyz.legacy.model.CuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {
    List<CuentaEntity> findByNombreIgnoreCase(String nombre);
    Optional<CuentaEntity> findFirstByCuentaId(Long cuentaId);
}