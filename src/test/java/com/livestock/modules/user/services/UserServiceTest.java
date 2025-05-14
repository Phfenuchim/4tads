package com.livestock.modules.user.services;

import com.livestock.modules.user.domain.role.Role;
import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.dto.UpdateUserDTO;
import com.livestock.modules.user.exceptions.UserInputException;
import com.livestock.modules.user.exceptions.UserNotFoundException;
import com.livestock.modules.user.repositories.RoleRepository;
import com.livestock.modules.user.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("João Gado");
        user.setEmail("joao@gado.com");
        user.setCpf("49051834802"); // Válido
        user.setPassword("senha123");
    }

    @Test
    void testCreateUserSuccessAndInvalidCpf() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        var createdUser = userService.createUser(user);
        assertNotNull(createdUser);
        assertTrue(createdUser.isActive());

        user.setCpf("invalid");
        assertThrows(UserInputException.class, () -> userService.createUser(user));
    }

    @Test
    void testUpdateUserPasswordSuccessAndInvalidId() {
        when(passwordEncoder.encode("newPass")).thenReturn("encoded");
        when(userRepository.updatePasswordUser(user.getId(), "encoded")).thenReturn(1);

        assertTrue(userService.updateUserPassword(user.getId(), "newPass"));

        assertThrows(UserInputException.class, () -> userService.updateUserPassword(null, "newPass"));
    }

    @Test
    void testUpdateUserActiveStatusSuccessAndInvalidId() {
        when(userRepository.updateActiveUser(user.getId(), true)).thenReturn(1);

        assertTrue(userService.updateUserActiveStatus(user.getId(), true));

        assertThrows(UserInputException.class, () -> userService.updateUserActiveStatus(null, false));
    }

    @Test
    void testUpdateUserSuccessAndRoleNotFound() {
        UUID roleId = UUID.randomUUID();
        UpdateUserDTO dto = new UpdateUserDTO("Novo Nome", "49051834802", roleId);
        Role role = new Role(roleId, "ROLE_CLIENTE");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        var updatedUser = userService.updateUser(user.getId(), dto);
        assertEquals(dto.getName(), updatedUser.getName());

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(user.getId(), dto));
    }

    @Test
    void testGetUserByIdSuccessAndNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        var foundUser = userService.getUserById(user.getId());
        assertEquals(user.getId(), foundUser.getId());

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(UUID.randomUUID()));
    }

    @Test
    void testGetAllRolesSuccessAndNotFound() {
        when(roleRepository.findAll()).thenReturn(Collections.singletonList(new Role(UUID.randomUUID(), "ROLE_USER")));
        var roles = userService.getAllRoles();
        assertFalse(roles.isEmpty());

        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(UserNotFoundException.class, () -> userService.getAllRoles());
    }
}
