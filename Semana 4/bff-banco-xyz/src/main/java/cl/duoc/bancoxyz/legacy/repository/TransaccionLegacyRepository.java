package cl.duoc.bancoxyz.legacy.repository;

import cl.duoc.bancoxyz.legacy.model.TransaccionLegacyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionLegacyRepository extends JpaRepository<TransaccionLegacyEntity, Long> {
    List<TransaccionLegacyEntity> findByEstadoRegistro(String estadoRegistro);
}