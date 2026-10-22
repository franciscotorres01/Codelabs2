package com.denkitronik.clienteservice2.cliente.services;

import com.denkitronik.clienteservice2.cliente.entities.Cliente;
import com.denkitronik.clienteservice2.cliente.entities.Region;
import com.denkitronik.clienteservice2.cliente.exceptions.ClienteExistsException;
import com.denkitronik.clienteservice2.cliente.exceptions.ClienteNotFoundException;
import com.denkitronik.clienteservice2.cliente.repositories.IClienteDao;
import com.denkitronik.clienteservice2.cliente.repositories.IRegionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteServiceImpl implements IClienteService {

    @Autowired
    private IClienteDao clienteDao;

    @Autowired
    private IRegionDao regionDao;

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> findAll(Pageable pageable) {
        return clienteDao.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente findById(Long id) {
        return clienteDao.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
    }

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        return guardar(cliente);
    }

    @Override
    @Transactional
    public Cliente update(Long id, Cliente datos) {
        Cliente existente = findById(id); // lanza 404 si no existe
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setEmail(datos.getEmail());
        existente.setRegion(datos.getRegion());
        return guardar(existente);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Cliente cliente = findById(id); // lanza 404 si no existe
        clienteDao.delete(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Region> findAllRegiones() {
        return regionDao.findAll();
    }

    // Transforma el error de BD (email duplicado) en una excepción propia
    private Cliente guardar(Cliente cliente) {
        try {
            return clienteDao.save(cliente);
        } catch (DataIntegrityViolationException e) {
            throw new ClienteExistsException(
                    "Ya existe un cliente con el email: " + cliente.getEmail());
        }
    }
}