package com.gado.api.repositories;

import com.gado.api.domain.user.Usr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsrRepository extends JpaRepository<Usr, Long> {

    Optional<Usr> findByEmail(String email);

    boolean existsByEmail(String email);

    // Você pode adicionar métodos personalizados aqui, se necessário
    // Por exemplo:
    // List<User> findByRole(String role);
}
