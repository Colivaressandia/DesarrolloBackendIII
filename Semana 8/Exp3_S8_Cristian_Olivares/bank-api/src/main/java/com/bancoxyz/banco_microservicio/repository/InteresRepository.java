package com.bancoxyz.banco_microservicio.repository;

import com.bancoxyz.banco_microservicio.model.Interes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InteresRepository extends JpaRepository<Interes, Long> {
    List<Interes> findByCuentaId(Long cuentaId);
}