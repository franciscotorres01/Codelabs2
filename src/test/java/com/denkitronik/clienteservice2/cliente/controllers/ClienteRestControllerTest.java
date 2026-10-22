package com.denkitronik.clienteservice2.cliente.controllers;

import com.denkitronik.clienteservice2.cliente.entities.Cliente;
import com.denkitronik.clienteservice2.cliente.entities.Region;
import com.denkitronik.clienteservice2.cliente.exceptions.ClienteNotFoundException;
import com.denkitronik.clienteservice2.cliente.services.IClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;                         // Spring Boot 3.x
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;  // Spring Boot 3.x
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteRestController.class)
@DisplayName("Integration tests (web slice) — ClienteRestController con MockMvc")
class ClienteRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean                                  // reemplaza IClienteService en el contexto de Spring
    private IClienteService clienteService;

    @Autowired
    private ObjectMapper objectMapper;            // para serializar objetos a JSON

    private Cliente cliente;
    private static final String BASE = "/api/v1/cliente-service/clientes";

    @BeforeEach
    void setUp() {
        Region region = new Region();
        region.setId(4L);
        region.setNombre("Europa");

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ada");
        cliente.setApellido("Lovelace");
        cliente.setEmail("ada@babbage.uk");
        cliente.setRegion(region);
    }

    // ---------- GET ----------

    @Test
    @DisplayName("GET /clientes → 200 con la página de clientes")
    void listarClientes_debeRetornar200ConPagina() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        when(clienteService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente), pageable, 1));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Ada"));
    }

    @Test
    @DisplayName("GET /clientes/1 → 200 con el cliente correcto")
    void buscarCliente_idExistente_debeRetornar200ConCliente() throws Exception {
        when(clienteService.findById(1L)).thenReturn(cliente);

        mockMvc.perform(get(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Ada"))
                .andExpect(jsonPath("$.apellido").value("Lovelace"));
    }

    @Test
    @DisplayName("GET /clientes/999 → 404 cuando el cliente no existe")
    void buscarCliente_idInexistente_debeRetornar404() throws Exception {
        when(clienteService.findById(999L))
                .thenThrow(new ClienteNotFoundException(999L));

        mockMvc.perform(get(BASE + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /clientes/regiones → 200 con la lista de regiones")
    void listarRegiones_debeRetornar200() throws Exception {
        when(clienteService.findAllRegiones()).thenReturn(List.of(cliente.getRegion()));

        mockMvc.perform(get(BASE + "/regiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Europa"));
    }

    // ---------- POST ----------

    @Test
    @DisplayName("POST /clientes válido → 201 Created")
    void crearCliente_valido_debeRetornar201() throws Exception {
        when(clienteService.save(any(Cliente.class))).thenReturn(cliente);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ada"));
    }

    @Test
    @DisplayName("POST /clientes con nombre vacío → 400 con el error del campo nombre")
    void crearCliente_nombreVacio_debeRetornar400() throws Exception {
        cliente.setNombre("");   // viola @NotEmpty @Size(min=2)

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nombre").exists());
    }

    @Test
    @DisplayName("POST /clientes con email inválido → 400")
    void crearCliente_emailInvalido_debeRetornar400() throws Exception {
        cliente.setEmail("esto-no-es-un-email");

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT ----------

    @Test
    @DisplayName("PUT /clientes/1 válido → 201 Created con el cliente actualizado")
    void actualizarCliente_valido_debeRetornar201() throws Exception {
        when(clienteService.findById(1L)).thenReturn(cliente);
        when(clienteService.save(any(Cliente.class))).thenReturn(cliente);

        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ada"));
    }

    @Test
    @DisplayName("PUT /clientes/999 → 404 cuando el cliente no existe")
    void actualizarCliente_idInexistente_debeRetornar404() throws Exception {
        when(clienteService.findById(999L))
                .thenThrow(new ClienteNotFoundException(999L));

        mockMvc.perform(put(BASE + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("DELETE /clientes/1 → 204 No Content")
    void eliminarCliente_idExistente_debeRetornar204() throws Exception {
        doNothing().when(clienteService).delete(1L);

        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /clientes/999 → 404 cuando el cliente no existe")
    void eliminarCliente_idInexistente_debeRetornar404() throws Exception {
        doThrow(new ClienteNotFoundException(999L)).when(clienteService).delete(999L);

        mockMvc.perform(delete(BASE + "/999"))
                .andExpect(status().isNotFound());
    }
}