package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.repositories.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    public Optional<Address> findById(UUID id) {
        return addressRepository.findById(id);
    }

    public List<Address> findByClientId(UUID clientId) {
        return addressRepository.findByClientId(clientId);
    }

    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public void deleteById(UUID id) {
        addressRepository.deleteById(id);
    }
}
