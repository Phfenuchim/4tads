package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.address.AddressType;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.dto.AddressDTO; // Importar se for usar
import com.livestock.modules.client.dto.ChangePasswordDTO;
import com.livestock.modules.client.dto.CreateClientDTO;
import com.livestock.modules.client.dto.NewAddressDTO;
import com.livestock.modules.client.repositories.AddressRepository; // Mockar AddressRepository
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
import com.livestock.modules.order.repositories.OrderRepository;
// import com.livestock.modules.user.validators.UserValidator; // Removido do ClientService, não precisa aqui
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private AddressRepository addressRepository; // Adicionado Mock
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ConsultaCepAPI consultaCepAPI;
    @Mock
    private OrderRepository orderRepository; // Mantido, mas pode ser removido se findOrdersByClient usar apenas clientRepository

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private CreateClientDTO createClientDto; // Renomeado para clareza
    private NewAddressDTO newAddressDto;     // Para o teste de adicionar novo endereço

    @Captor
    private ArgumentCaptor<List<Address>> addressListCaptor; // Captor para a lista de endereços

    @Captor
    private ArgumentCaptor<Address> addressCaptor; // Captor para um único endereço

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(UUID.randomUUID());
        client.setFirstName("João"); // Assumindo que você tem firstName e lastName
        client.setLastName("Gado");
        client.setFullName("João Gado");
        client.setCpf("123.456.789-00"); // Use um CPF válido para o UserValidator se ele for chamado
        client.setEmail("joao@gado.com");
        client.setPassword("senha123"); // Senha não hasheada para o objeto client base
        client.setStatus(true);
        client.setAddress(new ArrayList<>()); // Se a entidade Client tiver uma lista de endereços

        // DTO para criar cliente
        createClientDto = new CreateClientDTO();
        createClientDto.setFullName("João Gado Completo");
        createClientDto.setCpf("123.456.789-00");
        createClientDto.setEmail("joao.novo@gado.com");
        createClientDto.setPhone("(11) 98888-7777");
        createClientDto.setPassword("novaSenha123");
        createClientDto.setGender("M");
        createClientDto.setDateBirth(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 365 * 20)); // 20 anos atrás

        // Adicionar um endereço inicial ao DTO de criação
        AddressDTO initialAddressData = new AddressDTO(
                "01001-000", "Praça da Sé", "1", "Lado Ímpar", "Sé",
                "São Paulo", "SP", "Brasil"
                // Os campos 'isDefault' e 'type' foram removidos do AddressDTO quando usado para 'initialAddress'
        );
        createClientDto.setInitialAddress(initialAddressData);


        // DTO para adicionar novo endereço
        newAddressDto = new NewAddressDTO(
                "04550-000", "Rua Funchal", "500", "Andar 10", "Vila Olímpia",
                "São Paulo", "SP", "Brasil"
                // 'isDefault' e 'type' removidos do construtor do NewAddressDTO
        );
    }

    @Test
    void testCreateClientSuccess_ShouldCreateTwoAddresses() {
        // Arrange
        when(clientRepository.existsByEmail(createClientDto.getEmail())).thenReturn(false);
        when(clientRepository.existsByCpf(createClientDto.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(createClientDto.getPassword())).thenReturn("encodedPassword");

        CepResultDTO cepResult = new CepResultDTO();
        cepResult.setCep(createClientDto.getInitialAddress().getCep());
        cepResult.setLogradouro(createClientDto.getInitialAddress().getStreet());
        // ... preencher outros campos do cepResult se necessário para o service
        when(consultaCepAPI.consultaCep(createClientDto.getInitialAddress().getCep())).thenReturn(cepResult);

        // Quando o clientRepository.save é chamado, retornamos o 'client' mockado com um ID
        // Para simular que o cliente foi salvo e tem um ID para associar aos endereços
        Client savedClientShell = new Client(); // Um cliente "casca" só com o ID
        UUID clientId = UUID.randomUUID();
        savedClientShell.setId(clientId);

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client clientToSave = invocation.getArgument(0);
            clientToSave.setId(clientId); // Simula o ID gerado pelo banco
            return clientToSave;
        });

        // Quando addressRepository.saveAll é chamado, apenas verificamos
        when(addressRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));


        // Act
        Client resultClient = clientService.createClient(createClientDto);

        // Assert
        assertNotNull(resultClient);
        assertEquals(clientId, resultClient.getId());
        assertEquals(createClientDto.getFullName(), resultClient.getFullName());
        assertEquals("encodedPassword", resultClient.getPassword());

        verify(clientRepository).save(any(Client.class)); // Verifica se o cliente foi salvo
        verify(consultaCepAPI).consultaCep(createClientDto.getInitialAddress().getCep()); // Verifica consulta de CEP

        // Verificar se addressRepository.saveAll foi chamado com uma lista de 2 endereços
        verify(addressRepository).saveAll(addressListCaptor.capture());
        List<Address> savedAddresses = addressListCaptor.getValue();
        assertNotNull(savedAddresses);
        assertEquals(2, savedAddresses.size(), "Deveriam ser salvos dois endereços (ENTREGA e FATURAMENTO)");

        // Verificar os tipos dos endereços salvos
        assertTrue(savedAddresses.stream().anyMatch(addr -> addr.getType() == AddressType.ENTREGA && addr.getCep().equals(createClientDto.getInitialAddress().getCep())), "Deve haver um endereço de ENTREGA");
        assertTrue(savedAddresses.stream().anyMatch(addr -> addr.getType() == AddressType.FATURAMENTO && addr.getCep().equals(createClientDto.getInitialAddress().getCep())), "Deve haver um endereço de FATURAMENTO");

        // Verificar se os endereços estão associados ao cliente correto
        savedAddresses.forEach(addr -> assertEquals(clientId, addr.getClient().getId()));
    }

    @Test
    void testChangePasswordSuccess() {
        // Arrange
        ChangePasswordDTO changeDto = new ChangePasswordDTO();
        changeDto.setCurrentPassword("senha123"); // Senha original do 'client' mockado
        changeDto.setNewPassword("newPass");
        changeDto.setConfirmPassword("newPass");

        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("senha123", "senha123")).thenReturn(true); // Simula que a senha atual confere
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        // Act
        clientService.changePassword(client.getEmail(), changeDto);

        // Assert
        verify(passwordEncoder).matches("senha123", "senha123");
        verify(passwordEncoder).encode("newPass");
        verify(clientRepository).save(clientCaptor.capture()); // Usar um captor para Client
        Client savedClient = clientCaptor.getValue();
        assertEquals("encodedNewPass", savedClient.getPassword());
    }

    @Captor
    private ArgumentCaptor<Client> clientCaptor;


    @Test
    void testAddNewAddressSuccess_ShouldBeEntregaType() {
        // Arrange
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));

        CepResultDTO cepResult = new CepResultDTO();
        cepResult.setCep(newAddressDto.getCep());
        // ... preencher outros campos do cepResult se necessário
        when(consultaCepAPI.consultaCep(newAddressDto.getCep())).thenReturn(cepResult);

        // Quando addressRepository.save é chamado, apenas verificamos
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Address addedAddress = clientService.addNewAddress(client.getEmail(), newAddressDto);

        // Assert
        assertNotNull(addedAddress);
        assertEquals(newAddressDto.getCep(), addedAddress.getCep());
        assertEquals(newAddressDto.getStreet(), addedAddress.getStreet());
        assertEquals(AddressType.ENTREGA, addedAddress.getType(), "Novo endereço deve ser sempre do tipo ENTREGA");
        // defaultAddress não existe mais para ser verificado

        verify(addressRepository).save(addressCaptor.capture());
        Address savedAddressByRepo = addressCaptor.getValue();
        assertEquals(AddressType.ENTREGA, savedAddressByRepo.getType());
        assertEquals(client.getId(), savedAddressByRepo.getClient().getId());
    }

    // O teste testSetDefaultAddressSuccess DEVE SER REMOVIDO pois a funcionalidade foi removida.

    @Test
    void testGetClientOrders() { // Renomeado para findOrdersByClient se você mudou o nome no serviço
        // Arrange
        UUID clientId = client.getId();
        List<Order> mockOrders = List.of(new Order(), new Order());
        when(orderRepository.findByUserId(clientId)).thenReturn(mockOrders); // Assumindo que findByUserId existe

        // Act
        List<Order> orders = clientService.findOrdersByClient(clientId); // Ajuste o nome do método se necessário

        // Assert
        assertNotNull(orders);
        assertEquals(2, orders.size());
        verify(orderRepository).findByUserId(clientId);
    }

    // Adicionar testes para casos de falha (ex: email já existe, CPF inválido, CEP não encontrado, etc.)
    @Test
    void testCreateClient_WhenEmailExists_ShouldThrowException() {
        when(clientRepository.existsByEmail(createClientDto.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.createClient(createClientDto);
        });
        assertEquals("Email já cadastrado", exception.getMessage());
    }

    @Test
    void testCreateClient_WhenCpfExists_ShouldThrowException() {
        when(clientRepository.existsByEmail(createClientDto.getEmail())).thenReturn(false);
        when(clientRepository.existsByCpf(createClientDto.getCpf())).thenReturn(true);
        // UserValidator.isValidCPF é estático, não precisa mockar a menos que queira testar ele separadamente
        // ou se ele for chamado dentro do createClient e você quiser controlar seu retorno para este teste específico.
        // Para este caso, o mock de existsByCpf já cobre o cenário.

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.createClient(createClientDto);
        });
        assertEquals("CPF já cadastrado", exception.getMessage());
    }

    @Test
    void testCreateClient_WhenInvalidCpfFormat_ShouldThrowException() {
        // Arrange
        createClientDto.setCpf("12345"); // CPF com formato inválido
        when(clientRepository.existsByEmail(createClientDto.getEmail())).thenReturn(false);
        // Não precisa mockar existsByCpf aqui, pois a validação do formato deve ocorrer antes

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.createClient(createClientDto);
        });
        assertEquals("CPF inválido", exception.getMessage()); // Mensagem do UserValidator.isValidCPF
    }


    @Test
    void testAddNewAddress_WhenCepNotFound_ShouldThrowException() {
        when(clientRepository.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(consultaCepAPI.consultaCep(newAddressDto.getCep())).thenReturn(null); // Simula CEP não encontrado

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.addNewAddress(client.getEmail(), newAddressDto);
        });
        assertEquals("CEP inválido", exception.getMessage());
    }
}
