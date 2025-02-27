package com.livestock.modules.user.services;

import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.dto.UpdateUserDTO;
import com.livestock.modules.user.exceptions.UserInputException;
import com.livestock.modules.user.exceptions.UserNotFoundException;
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

    public User createUser(User user) {
        if (!UserValidator.isValidCPF(user.getCpf())) {
            throw new UserInputException("Seu CPF é inválido!");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserInputException("Este email já está em uso!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Page<User> getAllUsersPaginated(int pageNumber, int pageSize) {
        var users = this.userRepository.findAll(PageRequest.of(pageNumber, pageSize));

        if (users.isEmpty() || users == null) {
            throw new UserNotFoundException("Nenhum usuário encontrado!");
        }

        return users;
    }

    public List<User> findAllUsersByNameFilter(String name) {
        var users = this.userRepository.findAllByNameContaining(name);

        if (users.isEmpty() || users == null) {
            throw new UserNotFoundException("Nenhum usuário encontrado!");
        }

        return users;
    }

    public boolean updateUserActiveStatus(UUID id, boolean active) {
        if (id == null) {
            throw new UserInputException("ID do usuário não pode ser nulo!");
        }

        var lines = userRepository.updateActiveUser(id, active);

        if (lines == 0 ) {
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
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com o ID: " + id));

        if (updateUserDTO.getName() != null) {
            user.setName(updateUserDTO.getName());
        }

        if (updateUserDTO.getCpf() != null) {
            user.setCpf(updateUserDTO.getCpf());
        }

        return userRepository.save(user);
    }

}
