package com.bancoxyz.cuentas_api.repository;

import com.bancoxyz.cuentas_api.model.CuentaBancaria;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {

    List<CuentaBancaria> findByClienteId(Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CuentaBancaria c where c.id = :id")
    Optional<CuentaBancaria> findLockedById(@Param("id") Long id);
}
