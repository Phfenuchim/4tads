package com.gado.api.service;

import com.gado.api.domain.user.Usr;
import com.gado.api.repositories.UsrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UsrRepository usrRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUser(Usr usr) {
        if (!isValidCPF(usr.getCpf())) {
            throw new IllegalArgumentException("CPF inválido");
        }

        usr.setPassword(passwordEncoder.encode(usr.getPassword()));
        usrRepository.save(usr);
    }


    public boolean isValidCPF(String cpf) {
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^0-9]", "");

        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Calcula os dígitos verificadores
        int[] numbers = new int[11];
        for (int i = 0; i < 11; i++) {
            numbers[i] = Character.getNumericValue(cpf.charAt(i));
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += numbers[i] * (10 - i);
        }

        int remainder = sum % 11;
        int digit1 = (remainder < 2) ? 0 : 11 - remainder;

        if (numbers[9] != digit1) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += numbers[i] * (11 - i);
        }

        remainder = sum % 11;
        int digit2 = (remainder < 2) ? 0 : 11 - remainder;

        return numbers[10] == digit2;
    }

}
