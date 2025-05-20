package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.address.AddressType;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.dto.*;
import com.livestock.modules.client.repositories.AddressRepository;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
import com.livestock.modules.order.repositories.OrderRepository;
import com.livestock.modules.user.validators.UserValidator;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Avisar: Não usado diretamente aqui.
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConsultaCepAPI consultaCepAPI;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;

    // Construtor para injeção de dependências.
    public ClientService(ClientRepository clientRepository,
                         PasswordEncoder passwordEncoder,
                         ConsultaCepAPI consultaCepAPI,
                         OrderRepository orderRepository,
                         AddressRepository addressRepository) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.consultaCepAPI = consultaCepAPI;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Client createClient(CreateClientDTO createClientDTO) {
        if (clientRepository.existsByEmail(createClientDTO.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if (!UserValidator.isValidCPF(createClientDTO.getCpf())) {
            throw new IllegalArgumentException("CPF inválido");
        }
        if (clientRepository.existsByCpf(createClientDTO.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        String[] nameParts = createClientDTO.getFullName().trim().split("\\s+");
        if (nameParts.length < 2 || nameParts[0].length() < 3 || nameParts[1].length() < 3) {
            throw new IllegalArgumentException("O nome deve ter pelo menos duas palavras com no mínimo 3 letras cada");
        }

        Client client = new Client();
        client.setFirstName(nameParts[0]);
        client.setLastName(String.join(" ", java.util.Arrays.copyOfRange(nameParts, 1, nameParts.length)));
        client.setFullName(createClientDTO.getFullName());
        client.setCpf(createClientDTO.getCpf());
        client.setEmail(createClientDTO.getEmail());
        client.setPhone(createClientDTO.getPhone());
        client.setDate_birth(createClientDTO.getDateBirth());
        client.setStatus(true);
        client.setGender(createClientDTO.getGender());
        String encodedPassword = passwordEncoder.encode(createClientDTO.getPassword());
        client.setPassword(encodedPassword);

        Client savedClient = clientRepository.save(client);

        AddressDTO initialAddressData = createClientDTO.getInitialAddress();
        if (initialAddressData != null) {
            CepResultDTO cepResult = consultaCepAPI.consultaCep(initialAddressData.getCep());
            if (cepResult == null || cepResult.getCep() == null) {
                throw new IllegalArgumentException("CEP do endereço inicial inválido");
            }

            List<Address> addresses = new ArrayList<>();
            Address deliveryAddress = new Address();
            mapDtoToAddress(initialAddressData, deliveryAddress, savedClient, AddressType.ENTREGA);
            addresses.add(deliveryAddress);

            Address billingAddress = new Address();
            mapDtoToAddress(initialAddressData, billingAddress, savedClient, AddressType.FATURAMENTO);
            addresses.add(billingAddress);

            // Avisar: A linha savedClient.setAddress(addresses); pode ser redundante se a relação é
            // gerenciada pelo Address e CascadeType.ALL/PERSIST não está sendo usado do lado Client
            // para persistir novos Addresses apenas salvando o Client.
            // O addressRepository.saveAll(addresses) já persiste os endereços com a FK correta.
            // Se a entidade Client tem @OneToMany(mappedBy="client", cascade=CascadeType.ALL), então
            // adicionar à lista e salvar o client também funcionaria para persistir Address, mas
            // salvar explicitamente os Address é mais claro.
            addressRepository.saveAll(addresses);
            // Se você quiser que o objeto savedClient retornado tenha a lista de endereços preenchida
            // e você não está buscando-os novamente do banco antes de retornar, esta linha é útil.
            // No entanto, se a relação for EAGER fetched ou se você buscar os endereços do cliente
            // antes de usá-los, esta linha pode não ser estritamente necessária para a persistência dos endereços.
            // Considerando a sua entidade Client, ela tem @OneToMany(mappedBy = "client", cascade = CascadeType.ALL),
            // então, neste caso, a linha savedClient.setAddress(addresses) seguida por um (implícito ou explícito)
            // save do client poderia persistir os endereços, mas como você já faz addressRepository.saveAll,
            // esta linha serve mais para atualizar o objeto 'savedClient' em memória, se ele for usado posteriormente
            // na mesma transação com a expectativa de que a lista de endereços esteja populada.
            savedClient.setAddress(addresses);
        }
        return savedClient;
    }

    private void mapDtoToAddress(AddressDTO dto, Address address, Client client, AddressType type) {
        address.setClient(client);
        address.setCep(dto.getCep());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setDistrict(dto.getDistrict());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setType(type);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordDTO dto) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), client.getPassword())) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("A nova senha e a confirmação não coincidem.");
        }
        client.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        clientRepository.save(client);
    }

    @Transactional
    public Address addNewAddress(String email, @Valid NewAddressDTO dto) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    // Removido System.err.println para manter a classe mais limpa.
                    return new IllegalArgumentException("Cliente não encontrado");
                });

        CepResultDTO cepResult = consultaCepAPI.consultaCep(dto.getCep());
        if (cepResult == null || cepResult.getCep() == null) {
            // Removido System.err.println.
            throw new IllegalArgumentException("CEP inválido");
        }

        Address newAddress = new Address();
        newAddress.setClient(client);
        newAddress.setCep(dto.getCep());
        newAddress.setStreet(dto.getStreet());
        newAddress.setNumber(dto.getNumber());
        newAddress.setComplement(dto.getComplement());
        newAddress.setDistrict(dto.getDistrict());
        newAddress.setCity(dto.getCity());
        newAddress.setState(dto.getState());
        newAddress.setCountry(dto.getCountry());
        newAddress.setType(AddressType.ENTREGA);

        // Avisar: O try-catch aqui é redundante se o método já é @Transactional.
        // Se addressRepository.save() lançar uma exceção (ex: DataIntegrityViolationException),
        // a transação será automaticamente marcada para rollback.
        // Removendo o try-catch para simplificar, a menos que você tenha um tratamento específico
        // de exceção que precise ser feito aqui antes de relançar.
        // try {
        return addressRepository.save(newAddress);
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     throw e;
        // }
    }

    public List<Order> findOrdersByClient(UUID clientId) {
        return orderRepository.findByUserId(clientId);
    }

    public List<Address> findAddressesByClientId(UUID clientId){
        return addressRepository.findByClientId(clientId);
    }

    public Optional<Client> findClientByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    @Transactional
    public Client updateClientProfile(Client client, EditProfileDTO dto) {
        // Avisar: A entidade 'Client client' recebida como parâmetro aqui é gerenciada pelo JPA se
        // foi buscada anteriormente em uma transação. Se não, ela é 'detached'.
        // Para garantir que você está trabalhando com a entidade mais atual e gerenciada,
        // seria mais seguro buscar o cliente pelo ID aqui, similar ao que é feito em `updateUser` no `UserService`.
        // Ex: Client clientToUpdate = clientRepository.findById(client.getId()).orElseThrow(...);
        // E então aplicar as mudanças em 'clientToUpdate'.
        // No entanto, se o objeto 'client' já é o gerenciado, a abordagem atual funciona.

        String[] nameParts = dto.getFullName().trim().split("\\s+");
        if (nameParts.length < 2 || nameParts[0].length() < 3 || nameParts[1].length() < 3) {
            throw new IllegalArgumentException("O nome deve ter pelo menos duas palavras com no mínimo 3 letras cada");
        }
        client.setFirstName(nameParts[0]);
        client.setLastName(String.join(" ", java.util.Arrays.copyOfRange(nameParts, 1, nameParts.length)));
        client.setFullName(dto.getFullName());
        client.setDate_birth(dto.getDateBirth());
        client.setGender(dto.getGender());
        return clientRepository.save(client);
    }
}
