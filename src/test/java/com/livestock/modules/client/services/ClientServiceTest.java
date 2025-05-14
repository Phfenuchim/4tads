package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.address.AddressType;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.dto.ChangePasswordDTO;
import com.livestock.modules.client.dto.CreateClientDTO;
import com.livestock.modules.client.dto.NewAddressDTO;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
import com.livestock.modules.order.repositories.OrderRepository;
import com.livestock.modules.user.validators.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ConsultaCepAPI consultaCepAPI;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private ClientService clientService;

    private Client client;
    private CreateClientDTO dto;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(UUID.randomUUID());
        client.setFullName("João Gado");
        client.setCpf("490.518.348-02");
        client.setEmail("joao@gado.com");
        client.setPassword("senha123");
        client.setStatus(true);
        client.setAddress(new ArrayList<>());

        dto = new CreateClientDTO();
        dto.setFullName("João Gado");
        dto.setCpf("490.518.348-02");
        dto.setEmail("joao@gado.com");
        dto.setPhone("(11) 99999-9999");
        dto.setPassword("senha123");
        dto.setGender("M");
        dto.setDateBirth(new Date());
    }

    @Test
    void testCreateClientSuccess() {
        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(clientRepository.existsByCpf(dto.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        CepResultDTO cepResult = new CepResultDTO();
        cepResult.setCep("12345-678");
        when(consultaCepAPI.consultaCep(anyString())).thenReturn(cepResult);

        when(clientRepository.save(any(Client.class))).thenReturn(client);

        dto.setBillingAddress(new com.livestock.modules.client.dto.AddressDTO(
                "12345-678", "Rua 1", "10", null, "Centro", "Cidade", "SP", "Brasil", true, AddressType.FATURAMENTO
        ));

        var result = clientService.createClient(dto);

        assertNotNull(result);
        assertEquals("João Gado", result.getFullName());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testChangePasswordSuccess() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setCurrentPassword("oldPass");
        dto.setNewPassword("newPass");
        dto.setConfirmPassword("newPass");

        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPass");

        clientService.changePassword(client.getEmail(), dto);

        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testAddNewAddressSuccess() {
        NewAddressDTO dto = new NewAddressDTO(
                "12345-678", "Rua 2", "20", null, "Bairro", "Cidade", "SP", "Brasil", true, AddressType.ENTREGA
        );

        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));

        CepResultDTO cepResult = new CepResultDTO();
        cepResult.setCep("12345-678");
        when(consultaCepAPI.consultaCep(anyString())).thenReturn(cepResult);

        clientService.addNewAddress(client.getEmail(), dto);

        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testSetDefaultAddressSuccess() {
        Address addr1 = new Address();
        addr1.setId(UUID.randomUUID());
        addr1.setDefaultAddress(false);

        Address addr2 = new Address();
        addr2.setId(UUID.randomUUID());
        addr2.setDefaultAddress(false);

        client.setAddress(List.of(addr1, addr2));

        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));

        clientService.setDefaultAddress(client.getEmail(), addr2.getId());

        assertTrue(addr2.isDefaultAddress());
        assertFalse(addr1.isDefaultAddress());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testGetClientOrders() {
        client.setOrders(List.of(new Order(), new Order()));
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));

        var orders = clientService.getClientOrders(client.getEmail());

        assertEquals(2, orders.size());
    }
}
