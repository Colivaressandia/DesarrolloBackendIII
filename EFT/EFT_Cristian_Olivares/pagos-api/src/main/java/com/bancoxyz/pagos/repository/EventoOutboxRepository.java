package com.bancoxyz.pagos.repository;

import com.bancoxyz.pagos.model.EventoOutbox;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface EventoOutboxRepository extends JpaRepository<EventoOutbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EventoOutbox> findTop100ByPublicadoEnIsNullOrderByCreadoEnAsc();
}
