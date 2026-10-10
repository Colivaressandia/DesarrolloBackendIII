package com.bancoxyz.clientes.controller;

import com.bancoxyz.clientes.model.Cliente;
import com.bancoxyz.clientes.service.ClienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClientesController {

    private final ClienteService service;

    public ClientesController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Cliente> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente crear(@Valid @RequestBody ClienteRequest request) {
        return service.crear(request.toEntity());
    }

    @PutMapping("/{id}")
    public Cliente actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return service.actualizar(id, request.toEntity());
    }

    @PatchMapping("/{id}/estado")
    public Cliente cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return service.cambiarEstado(id, activo);
    }

    public record ClienteRequest(
            @NotBlank @Size(max = 80) String nombres,
            @NotBlank @Size(max = 80) String apellidos,
            @NotBlank @Pattern(regexp = "[0-9]{1,8}-[0-9Kk]", message = "RUT debe tener formato 12345678-9")
            String rut,
            @NotBlank @Email @Size(max = 150) String correo,
            @Size(max = 30) String telefono) {

        Cliente toEntity() {
            return new Cliente(nombres.trim(), apellidos.trim(), rut.trim(),
                    correo.trim().toLowerCase(), telefono == null ? null : telefono.trim());
        }
    }
}
