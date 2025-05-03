package com.livestock.modules.user.services;

import com.livestock.modules.user.domain.role.Role;
import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.dto.UpdateUserDTO;
import com.livestock.modules.user.exceptions.UserInputException;
import com.livestock.modules.user.exceptions.UserNotFoundException;
import com.livestock.modules.user.repositories.RoleRepository;
import com.livestock.modules.user.repositories.UserRepository;
import com.livestock.modules.user.validators.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public User createUser(User user) {
        if (!UserValidator.isValidCPF(user.getCpf())) {
            throw new UserInputException("Seu CPF é inválido!");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserInputException("Este email já está em uso!");
        }

        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Page<User> getAllUsersPaginated(int pageNumber, int pageSize) {
        return this.userRepository.findAll(PageRequest.of(pageNumber, pageSize));
    }


    public List<User> findAllUsersByNameFilter(String name) {
        return this.userRepository.findAllByNameContaining(name);
    }


    public boolean updateUserActiveStatus(UUID id, boolean active) {
        if (id == null) {
            throw new UserInputException("ID do usuário não pode ser nulo!");
        }

        var lines = userRepository.updateActiveUser(id, active);

        if (lines == 0) {
            throw new IllegalArgumentException("Não foi possivel ativar/desativar usuário!");
        }

        return true;
    }

    public boolean updateUserPassword(UUID id, String newPassword) {
        if (id == null) {
            throw new UserInputException("ID do usuário não pode ser nulo!");
        }

        var lines = userRepository.updatePasswordUser(id, passwordEncoder.encode(newPassword));

        if (lines == 0) {
            throw new IllegalArgumentException("Não foi possível atualizar a senha do usuário!");
        }

        return true;
    }

    public User updateUser(UUID id, UpdateUserDTO updateUserDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o ID: " + id));

        if (updateUserDTO.getName() != null) {
            user.setName(updateUserDTO.getName());
        }

        if (updateUserDTO.getCpf() != null) {
            if (!UserValidator.isValidCPF(updateUserDTO.getCpf())) {
                throw new UserInputException("CPF inválido");
            }
            user.setCpf(updateUserDTO.getCpf());
        }

        // Atualizar o role se for fornecido
        if (updateUserDTO.getRoleId() != null) {
            Role role = roleRepository.findById(updateUserDTO.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role não encontrado"));

            // Limpar roles existentes e adicionar o novo
            user.getRoles().clear();
            user.getRoles().add(role);
        }

        return userRepository.save(user);
    }


    public List<Role> getAllRoles() {
        var roles = this.roleRepository.findAll();

        if (roles.isEmpty()) {
            throw new UserNotFoundException("Nenhuma role encontrada!");
        }

        return roles;
    }

    public User getUserById(UUID id) {
        var user = userRepository.findById(id);

        if (!user.isPresent()) {
            throw new UserNotFoundException("Usuário não encontrado!");
        }

        return user.get();

    }

}
