package com.bancoxyz.clientes.service;

import com.bancoxyz.clientes.model.Cliente;
import com.bancoxyz.clientes.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:clientestest;DB_CLOSE_DELAY=-1"
})
class ClienteServiceTest {

    @Autowired
    private ClienteService service;

    @Autowired
    private ClienteRepository repository;

    @Test
    void createsUpdatesAndDeactivatesAClient() {
        Cliente created = service.crear(new Cliente(
                "Cristian", "Olivares", "12345678-9", "cristian@example.cl", "+56912345678"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.isActivo()).isTrue();

        Cliente updated = service.actualizar(created.getId(), new Cliente(
                "Cristian Andrés", "Olivares", "12345678-9", "cristian@example.cl", null));
        assertThat(updated.getNombres()).isEqualTo("Cristian Andrés");

        Cliente deactivated = service.cambiarEstado(created.getId(), false);
        assertThat(deactivated.isActivo()).isFalse();
    }

    @Test
    void returnsNotFoundForMissingClient() {
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.buscar(Long.MAX_VALUE))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void rejectsDuplicateIdentityAndEmail() {
        service.crear(new Cliente("Ana", "Pérez", "11111111-1", "ana@example.cl", null));

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.crear(new Cliente(
                        "Otra", "Persona", "11111111-1", "otra@example.cl", null)))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> service.crear(new Cliente(
                        "Otra", "Persona", "22222222-2", "ANA@example.cl", null)))
                .satisfies(error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
    }
}
