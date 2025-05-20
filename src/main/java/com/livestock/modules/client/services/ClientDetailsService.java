package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.ClientRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Removido: import java.util.Collection; // Não usado diretamente
import java.util.Collections;

@Service
public class ClientDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;

    // Construtor para injeção de dependência.
    public ClientDetailsService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado com o email: " + email)); // Avisar: Mensagem de erro um pouco mais informativa.

        // Avisar: Considerar verificar se o cliente está ativo aqui, se houver um campo 'status' ou 'active'.
        // if (client.getStatus() == null || !client.getStatus()) {
        //     throw new UsernameNotFoundException("Cliente inativo: " + email);
        // }

        return new org.springframework.security.core.userdetails.User(
                client.getEmail(),
                client.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")) // Avisar: Adicionado prefixo "ROLE_".
        );
    }
}
