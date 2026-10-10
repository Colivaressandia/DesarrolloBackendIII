package com.bancoxyz.cuentas_api.repository;

import com.bancoxyz.cuentas_api.model.TransferenciaProcesada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferenciaProcesadaRepository extends JpaRepository<TransferenciaProcesada, Long> {

    Optional<TransferenciaProcesada> findByClaveIdempotencia(String claveIdempotencia);
}
