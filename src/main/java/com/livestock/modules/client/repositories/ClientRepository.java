package com.livestock.modules.client.repositories;

import com.livestock.modules.client.domain.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
