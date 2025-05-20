package com.livestock.common.service;

import com.livestock.common.GadusUserDetails;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
// Removidas importações não utilizadas como SimpleGrantedAuthority, PasswordEncoder, Collectors

@Service
public class UnifiedUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    // Construtor para injeção de dependências
    public UnifiedUserDetailsService(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

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
