package com.bancoxyz.cuentas_api.repository;

import com.bancoxyz.cuentas_api.model.CuentaAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CuentaAnualRepository extends JpaRepository<CuentaAnual, Long> {
    List<CuentaAnual> findByCuentaId(Long cuentaId);
}