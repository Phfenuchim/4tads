package com.gado.api.repositories;

import com.gado.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Você pode adicionar métodos personalizados aqui, se necessário
    // Por exemplo:
    // List<User> findByRole(String role);
}
