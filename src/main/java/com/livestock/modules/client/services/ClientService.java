package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.address.AddressType;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.dto.*;
import com.livestock.modules.client.repositories.AddressRepository; // Importar
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
import com.livestock.modules.order.repositories.OrderRepository;
import com.livestock.modules.user.validators.UserValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConsultaCepAPI consultaCepAPI;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository; // Adicionado para salvar endereços


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

        // Salva o cliente primeiro para ter o ID para associar aos endereços
        Client savedClient = clientRepository.save(client);

        // Criar endereço inicial (que será tanto ENTREGA quanto FATURAMENTO)
        AddressDTO initialAddressData = createClientDTO.getInitialAddress();
        if (initialAddressData != null) {
            // Valida CEP
            CepResultDTO cepResult = consultaCepAPI.consultaCep(initialAddressData.getCep());
            if (cepResult == null || cepResult.getCep() == null) {
                // Idealmente, lançar uma exceção mais específica ou adicionar erro ao BindingResult se isso fosse no controller
                throw new IllegalArgumentException("CEP do endereço inicial inválido");
            }

            List<Address> addresses = new ArrayList<>();

            // Endereço de ENTREGA
            Address deliveryAddress = new Address();
            mapDtoToAddress(initialAddressData, deliveryAddress, savedClient, AddressType.ENTREGA);
            addresses.add(deliveryAddress);

            // Endereço de FATURAMENTO
            Address billingAddress = new Address();
            mapDtoToAddress(initialAddressData, billingAddress, savedClient, AddressType.FATURAMENTO);
            addresses.add(billingAddress);

            addressRepository.saveAll(addresses); // Salva os dois endereços
            savedClient.setAddress(addresses); // Associa ao cliente (se a relação for bidirecional e gerenciada pelo client)
            // Se a relação for unidirecional de Address para Client, apenas salvar os endereços é suficiente.
        }

        return savedClient; // Retorna o cliente com os endereços (se a relação for carregada)
    }

    // Método auxiliar para mapear DTO para Entidade Address
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
        // defaultAddress foi removido
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
                    System.err.println("addNewAddress - Cliente não encontrado para email: " + email);
                    return new IllegalArgumentException("Cliente não encontrado");
                });

        CepResultDTO cepResult = consultaCepAPI.consultaCep(dto.getCep());
        if (cepResult == null || cepResult.getCep() == null) {
            System.err.println("addNewAddress - CEP inválido: " + dto.getCep());
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

        try {
            Address savedAddress = addressRepository.save(newAddress);
            return savedAddress;
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Relançar para que a transação faça rollback
        }
    }


    public List<Order> findOrdersByClient(UUID clientId) {
        return orderRepository.findByUserId(clientId);
    }

    public List<Address> findAddressesByClientId(UUID clientId){ // Adicionado para o controller
        return addressRepository.findByClientId(clientId);
    }

    public Optional<Client> findClientByEmail(String email) { // Adicionado para o controller
        return clientRepository.findByEmail(email);
    }

    // Adicionar este método se você permitir que clientes atualizem seus perfis básicos
    @Transactional
    public Client updateClientProfile(Client client, EditProfileDTO dto) {
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
