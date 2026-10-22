package com.denkitronik.clienteservice2.cliente.controllers;

import com.denkitronik.clienteservice2.cliente.entities.Cliente;
import com.denkitronik.clienteservice2.cliente.entities.Region;
import com.denkitronik.clienteservice2.cliente.services.IClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Ruta exigida por la plataforma: /api/v1/cliente-service/clientes
@RestController
@RequestMapping("/api/${api.version}/cliente-service/clientes")
public class ClienteRestController {

    @Autowired
    private IClienteService clienteService;

    @GetMapping
    public Page<Cliente> index(Pageable pageable) {
        return clienteService.findAll(pageable);
    }

    @GetMapping("/regiones")
    public List<Region> listarRegiones() {
        return clienteService.findAllRegiones();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> show(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.findById(id)); // 404 vía excepción
    }

    @PostMapping
    public ResponseEntity<Cliente> create(@Valid @RequestBody Cliente cliente) {
        cliente.setId(null); // fuerza creación
        Cliente nuevo = clienteService.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> update(@Valid @RequestBody Cliente cliente, @PathVariable Long id) {
        Cliente actual = clienteService.findById(id); // 404 si no existe
        actual.setNombre(cliente.getNombre());
        actual.setApellido(cliente.getApellido());
        actual.setEmail(cliente.getEmail());
        actual.setCreateAt(cliente.getCreateAt());
        actual.setRegion(cliente.getRegion());
        // La plataforma exige 201 Created al actualizar
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.save(actual));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id); // 404 si no existe
        return ResponseEntity.noContent().build();
    }
}