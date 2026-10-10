package com.bancoxyz.clientes.service;

import com.bancoxyz.clientes.model.Cliente;
import com.bancoxyz.clientes.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    public Cliente crear(Cliente cliente) {
        if (repository.existsByRut(cliente.getRut())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con ese RUT");
        }
        if (repository.existsByCorreoIgnoreCase(cliente.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente con ese correo");
        }
        return repository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente datos) {
        Cliente cliente = buscar(id);
        boolean rutEnUso = repository.existsByRut(datos.getRut())
                && !cliente.getRut().equalsIgnoreCase(datos.getRut());
        boolean correoEnUso = repository.existsByCorreoIgnoreCase(datos.getCorreo())
                && !cliente.getCorreo().equalsIgnoreCase(datos.getCorreo());
        if (rutEnUso || correoEnUso) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUT o correo ya pertenece a otro cliente");
        }
        cliente.setNombres(datos.getNombres());
        cliente.setApellidos(datos.getApellidos());
        cliente.setRut(datos.getRut());
        cliente.setCorreo(datos.getCorreo());
        cliente.setTelefono(datos.getTelefono());
        return repository.save(cliente);
    }

    public Cliente cambiarEstado(Long id, boolean activo) {
        Cliente cliente = buscar(id);
        cliente.setActivo(activo);
        return repository.save(cliente);
    }
}
