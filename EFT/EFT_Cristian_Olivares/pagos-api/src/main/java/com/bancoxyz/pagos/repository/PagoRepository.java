package com.bancoxyz.pagos.repository;

import com.bancoxyz.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, String> {

    Optional<Pago> findByClaveIdempotencia(String claveIdempotencia);
}
