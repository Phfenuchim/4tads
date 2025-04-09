package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.dto.AddressDTO;
import com.livestock.modules.client.dto.CreateClientDTO;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConsultaCepAPI consultaCepAPI;

    @Transactional
    public Client createClient(CreateClientDTO createClientDTO) {
        // Verificar se o email já existe
        if (clientRepository.existsByEmail(createClientDTO.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
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
        String encodedPassword = passwordEncoder.encode(createClientDTO.getPassword());
        client.setPassword(encodedPassword);

        // Criar endereço de faturamento
        Address billingAddress = createAddress(createClientDTO.getBillingAddress(), client, "faturamento", true);

        List<Address> addresses = new ArrayList<>();
        addresses.add(billingAddress);

        // Adicionar endereços de entrega, se houver
        if (createClientDTO.getShippingAddresses() != null && !createClientDTO.getShippingAddresses().isEmpty()) {
            for (AddressDTO addressDTO : createClientDTO.getShippingAddresses()) {
                Address shippingAddress = createAddress(addressDTO, client, "entrega", false);
                addresses.add(shippingAddress);
            }
        }

        client.setAddress(addresses);

        return clientRepository.save(client);
    }

    private Address createAddress(AddressDTO addressDTO, Client client, String type, boolean isDefault) {
        // Validar CEP via API
        CepResultDTO cepResult = consultaCepAPI.consultaCep(addressDTO.getCep());
        if (cepResult == null || cepResult.getCep() == null) {
            throw new IllegalArgumentException("CEP inválido");
        }

        Address address = new Address();
        address.setClient(client);
        address.setStreet(addressDTO.getStreet());
        address.setNumber(addressDTO.getNumber());
        address.setComplement(addressDTO.getComplement());
        address.setDistrict(addressDTO.getDistrict());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.set_default(isDefault);

        return address;
    }
}
