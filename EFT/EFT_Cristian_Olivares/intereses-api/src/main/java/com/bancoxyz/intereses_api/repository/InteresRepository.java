package com.bancoxyz.intereses_api.repository;

import com.bancoxyz.intereses_api.model.Interes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InteresRepository extends JpaRepository<Interes, Long> {
    List<Interes> findByCuentaId(Long cuentaId);
    List<Interes> findByTipo(String tipo);
}