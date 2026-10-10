package com.bancoxyz.banco_microservicio.repository;

import com.bancoxyz.banco_microservicio.model.CuentaAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CuentaAnualRepository extends JpaRepository<CuentaAnual, Long> {
    List<CuentaAnual> findByCuentaId(Long cuentaId);
}