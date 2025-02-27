package com.livestock.modules.user.repositories;

import com.livestock.modules.user.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    List<User> findAllByNameContaining(String name);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("""
            UPDATE User u
            SET u.active = :active
            WHERE u.id = :id
            
            """)
    int updateActiveUser(@Param("id") UUID id, @Param("active") boolean active);


    @Transactional
    @Modifying
    @Query("""
            UPDATE User u
            SET u.password = :password
            WHERE u.id = :id
            
            """)
    int updatePasswordUser(@Param("id") UUID id, @Param("password") String password);

    // Você pode adicionar métodos personalizados aqui, se necessário
    // Por exemplo:
    // List<User> findByRole(String role);
}
