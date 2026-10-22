package com.denkitronik.clienteservice2.cliente.services;

import com.denkitronik.clienteservice2.cliente.entities.Cliente;
import com.denkitronik.clienteservice2.cliente.entities.Region;
import com.denkitronik.clienteservice2.cliente.exceptions.ClienteNotFoundException;
import com.denkitronik.clienteservice2.cliente.repositories.IClienteDao;
import com.denkitronik.clienteservice2.cliente.repositories.IRegionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteServiceImpl - Unit tests")
class ClienteServiceImplTest {

    @Mock
    private IClienteDao clienteDao;

    @Mock
    private IRegionDao regionDao;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private Region region;

    @BeforeEach
    void setUp() {
        region = new Region();
        region.setId(1L);
        region.setNombre("Sudamerica");

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana");
        cliente.setApellido("Torres");
        cliente.setEmail("ana@mail.com");
        cliente.setRegion(region);
    }

    // ---------- findById ----------

    @Test
    @DisplayName("findById: existe -> devuelve el cliente")
    void findById_existente_retornaCliente() {
        when(clienteDao.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.findById(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmail()).isEqualTo("ana@mail.com");
        verify(clienteDao).findById(1L);
    }

    @Test
    @DisplayName("findById: no existe -> lanza ClienteNotFoundException")
    void findById_noExiste_lanzaNotFound() {
        when(clienteDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(99L))
                .isInstanceOf(ClienteNotFoundException.class);
    }

    // ---------- findAll ----------

    @Test
    @DisplayName("findAll: devuelve la pagina del DAO")
    void findAll_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Cliente> pagina = new PageImpl<>(List.of(cliente), pageable, 1);
        when(clienteDao.findAll(pageable)).thenReturn(pagina);

        Page<Cliente> resultado = clienteService.findAll(pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        verify(clienteDao).findAll(pageable);
    }

    // ---------- save ----------

    @Test
    @DisplayName("save: guarda y devuelve el cliente")
    void save_guardaYRetornaCliente() {
        when(clienteDao.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.save(cliente);

        assertThat(resultado).isSameAs(cliente);
        verify(clienteDao).save(cliente);
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete: existe -> elimina el cliente")
    void delete_existente_eliminaCliente() {
        when(clienteDao.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.delete(1L);

        verify(clienteDao).delete(cliente);
    }

    @Test
    @DisplayName("delete: no existe -> lanza NotFound y nunca borra")
    void delete_noExiste_lanzaNotFoundYNoBorra() {
        when(clienteDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.delete(99L))
                .isInstanceOf(ClienteNotFoundException.class);

        verify(clienteDao, never()).delete(org.mockito.ArgumentMatchers.any(Cliente.class));
    }

    // ---------- findAllRegiones ----------

    @Test
    @DisplayName("findAllRegiones: devuelve la lista del DAO")
    void findAllRegiones_retornaLista() {
        when(regionDao.findAll()).thenReturn(List.of(region));

        List<Region> resultado = clienteService.findAllRegiones();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Sudamerica");
    }
}