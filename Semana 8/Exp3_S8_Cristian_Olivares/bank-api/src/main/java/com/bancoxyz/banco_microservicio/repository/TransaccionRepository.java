package com.bancoxyz.banco_microservicio.repository;

import com.bancoxyz.banco_microservicio.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByTipo(String tipo);
}