package com.bancoxyz.cuentas_api.service;

import com.bancoxyz.cuentas_api.model.CuentaBancaria;
import com.bancoxyz.cuentas_api.model.CuentaBancaria.TipoCuenta;
import com.bancoxyz.cuentas_api.repository.CuentaBancariaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
@Import(CuentaBancariaService.class)
class CuentaBancariaServiceTest {

    @Autowired
    private CuentaBancariaService service;

    @Autowired
    private CuentaBancariaRepository repository;

    @Test
    void opensMaintainsAndClosesAnAccount() {
        CuentaBancaria created = service.abrir(25L, TipoCuenta.AHORRO, new BigDecimal("1250.00"));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getEstado()).isEqualTo(CuentaBancaria.EstadoCuenta.ACTIVA);

        CuentaBancaria maintained = service.mantener(created.getId(), 26L, TipoCuenta.CORRIENTE);
        assertThat(maintained.getClienteId()).isEqualTo(26L);
        assertThat(maintained.getTipo()).isEqualTo(TipoCuenta.CORRIENTE);

        CuentaBancaria closed = service.cerrar(created.getId());
        assertThat(closed.getEstado()).isEqualTo(CuentaBancaria.EstadoCuenta.CERRADA);
        assertThat(repository.findById(created.getId())).contains(closed);

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.mantener(created.getId(), 27L, TipoCuenta.AHORRO))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void rejectsNegativeOpeningBalance() {
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.abrir(25L, TipoCuenta.AHORRO, new BigDecimal("-1.00")))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void transfersAtomicallyAndDoesNotRepeatAnIdempotentRequest() {
        CuentaBancaria source = service.abrir(25L, TipoCuenta.CORRIENTE, new BigDecimal("500.00"));
        CuentaBancaria destination = service.abrir(26L, TipoCuenta.AHORRO, new BigDecimal("10.00"));

        var first = service.transferir(source.getId(), destination.getId(),
                new BigDecimal("125.00"), "transfer-eft-001");
        var retry = service.transferir(source.getId(), destination.getId(),
                new BigDecimal("125.00"), "transfer-eft-001");

        assertThat(first.repetida()).isFalse();
        assertThat(retry.repetida()).isTrue();
        assertThat(service.buscar(source.getId()).getSaldo()).isEqualByComparingTo("375.00");
        assertThat(service.buscar(destination.getId()).getSaldo()).isEqualByComparingTo("135.00");
    }

    @Test
    void doesNotChangeBalancesWhenTransferHasInsufficientFunds() {
        CuentaBancaria source = service.abrir(25L, TipoCuenta.CORRIENTE, new BigDecimal("20.00"));
        CuentaBancaria destination = service.abrir(26L, TipoCuenta.AHORRO, new BigDecimal("10.00"));

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.transferir(source.getId(), destination.getId(),
                        new BigDecimal("125.00"), "transfer-eft-002"))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(422));
        assertThat(service.buscar(source.getId()).getSaldo()).isEqualByComparingTo("20.00");
        assertThat(service.buscar(destination.getId()).getSaldo()).isEqualByComparingTo("10.00");
    }

    @Test
    void rejectsReusingAnIdempotencyKeyForDifferentTransferDetails() {
        CuentaBancaria source = service.abrir(25L, TipoCuenta.CORRIENTE, new BigDecimal("500.00"));
        CuentaBancaria destination = service.abrir(26L, TipoCuenta.AHORRO, BigDecimal.ZERO);
        service.transferir(source.getId(), destination.getId(),
                new BigDecimal("125.00"), "transfer-eft-003");

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.transferir(source.getId(), destination.getId(),
                        new BigDecimal("126.00"), "transfer-eft-003"))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
        assertThat(service.buscar(source.getId()).getSaldo()).isEqualByComparingTo("375.00");
        assertThat(service.buscar(destination.getId()).getSaldo()).isEqualByComparingTo("125.00");
    }
}
