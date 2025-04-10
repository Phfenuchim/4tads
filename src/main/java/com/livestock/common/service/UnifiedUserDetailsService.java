package com.livestock.common.service;

import com.livestock.common.GadusUserDetails;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UnifiedUserDetailsService implements UserDetailsService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Primeiro tenta encontrar como usuário administrativo
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isPresent()) {
            return new GadusUserDetails(userOpt.get());
        }

        // Se não encontrar como usuário administrativo, tenta como cliente
        Optional<Client> clientOpt = clientRepository.findByEmail(username);
        if (clientOpt.isPresent()) {
            return new GadusUserDetails(clientOpt.get());
        }

        // Se não encontrar em nenhum lugar, lança exceção
        throw new UsernameNotFoundException("Usuário não encontrado: " + username);
    }
}




