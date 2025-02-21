package com.livestock.modules.user.services;

import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.exceptions.UserInputException;
import com.livestock.modules.user.repositories.UserRepository;
import com.livestock.modules.user.validators.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


}
