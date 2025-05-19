package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.address.AddressType;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.dto.AddressDTO;
import com.livestock.modules.client.dto.ChangePasswordDTO;
import com.livestock.modules.client.dto.CreateClientDTO;
import com.livestock.modules.client.dto.NewAddressDTO;
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


    @Transactional
    public Client createClient(CreateClientDTO createClientDTO) {
        // Verificar se o email já existe
        if (clientRepository.existsByEmail(createClientDTO.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        // Validar CPF usando o UserValidator - ADICIONAR ESTA PARTE
        if (!UserValidator.isValidCPF(createClientDTO.getCpf())) {
            throw new IllegalArgumentException("CPF inválido");
        }

        // Verificar se o CPF já existe
        if (clientRepository.existsByCpf(createClientDTO.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        // Validar nome completo
        String[] nameParts = createClientDTO.getFullName().trim().split("\\s+");
        if (nameParts.length < 2 || nameParts[0].length() < 3 || nameParts[1].length() < 3) {
            throw new IllegalArgumentException("O nome deve ter pelo menos duas palavras com no mínimo 3 letras cada");
        }

        // Criar cliente
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

        // Criar endereço de faturamento
        Address billingAddress = createAddress(createClientDTO.getBillingAddress(), client, AddressType.FATURAMENTO, true);

        List<Address> addresses = new ArrayList<>();
        addresses.add(billingAddress);

        // Adicionar endereços de entrega, se houver
        if (createClientDTO.getShippingAddresses() != null && !createClientDTO.getShippingAddresses().isEmpty()) {
            for (AddressDTO addressDTO : createClientDTO.getShippingAddresses()) {
                Address shippingAddress = createAddress(addressDTO, client, AddressType.ENTREGA, false);
                addresses.add(shippingAddress);
            }
        }

        client.setAddress(addresses);

        return clientRepository.save(client);
    }

    private Address createAddress(AddressDTO addressDTO, Client client, AddressType type, boolean isDefault){
        CepResultDTO cepResult = consultaCepAPI.consultaCep(addressDTO.getCep());
        if (cepResult == null || cepResult.getCep() == null) {
            throw new IllegalArgumentException("CEP inválido");
        }

        Address address = new Address();
        address.setClient(client);
        address.setCep(addressDTO.getCep());
        address.setStreet(addressDTO.getStreet());
        address.setNumber(addressDTO.getNumber());
        address.setComplement(addressDTO.getComplement());
        address.setDistrict(addressDTO.getDistrict());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setDefaultAddress(isDefault);
        address.setType(addressDTO.getType()); // ✅ ESSENCIAL

        return address;
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
    public void addNewAddress(String email, @Valid NewAddressDTO dto) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        // Se for tipo FATURAMENTO, impedir caso já tenha um
        if (dto.getType() == AddressType.FATURAMENTO) {
            boolean alreadyHasBilling = client.getAddress().stream()
                    .anyMatch(a -> a.getType() == AddressType.FATURAMENTO);
            if (alreadyHasBilling) {
                throw new IllegalArgumentException("Você já possui um endereço de faturamento.");
            }
        }

        CepResultDTO cepResult = consultaCepAPI.consultaCep(dto.getCep());
        if (cepResult == null || cepResult.getCep() == null) {
            throw new IllegalArgumentException("CEP inválido");
        }

        if (dto.isDefault()) {
            client.getAddress().forEach(addr -> addr.setDefaultAddress(false));
        }

        Address address = new Address();
        address.setClient(client);
        address.setCep(dto.getCep());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setDistrict(dto.getDistrict());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setDefaultAddress(dto.isDefault());
        address.setType(dto.getType());

        client.getAddress().add(address);
        clientRepository.save(client);
    }


    @Transactional
    public void setDefaultAddress(String email, UUID addressId) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));

        Address selectedAddress = client.getAddress().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado"));

        // Desmarcar todos os outros
        client.getAddress().forEach(addr -> addr.setDefaultAddress(false));

        // Marcar o selecionado
        selectedAddress.setDefaultAddress(true);

        clientRepository.save(client);
    }

    public List<Order> getClientOrders(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));
        return client.getOrders(); // ou consulte um OrderRepository se preferir
    }

    public List<Order> findOrdersByClient(UUID clientId) {
        return orderRepository.findByUserId(clientId);
    }




}
