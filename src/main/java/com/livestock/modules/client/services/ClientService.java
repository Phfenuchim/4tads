package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Optional<Client> findById(UUID id) {
        return clientRepository.findById(id);
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public void deleteById(UUID id) {
        clientRepository.deleteById(id);
    }
}
