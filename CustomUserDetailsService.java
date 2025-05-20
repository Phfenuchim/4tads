package com.livestock.modules.user.services;

import com.livestock.modules.user.domain.role.Role;
import com.livestock.modules.user.domain.user.User;
import com.livestock.modules.user.exceptions.UserNotFoundException; // Avisar: Esta exceção é sua, não a do Spring Security.
import com.livestock.modules.user.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Avisar: Esta é a exceção esperada pelo Spring Security.
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Construtor para injeção de dependência.
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException { // Avisar: Assinatura do método espera UsernameNotFoundException.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email)); // Avisar: Usar UsernameNotFoundException aqui.

        if (!user.isActive()) {
            // Avisar: Spring Security espera UsernameNotFoundException (ou uma subclasse dela) se o usuário não puder ser autenticado.
            // Lançar uma exceção diferente pode não ser tratado corretamente pelo framework de segurança.
            // Você pode querer logar que o usuário está inativo, mas ainda lançar UsernameNotFoundException
            // ou uma exceção específica que o Spring Security entenda (como DisabledException, LockedException).
            // Para simplificar, mantendo a lógica de "não encontrado" se inativo:
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(Role::getName).map(roleName -> "ROLE_" + roleName).toArray(String[]::new)) // Avisar: Adicionado prefixo "ROLE_" e usando .authorities()
                // .roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new)) // O método .roles() adiciona "ROLE_" automaticamente.
                // Se seus nomes de Role já incluem "ROLE_", então .roles() está correto.
                // Se não incluem, .authorities() com o prefixo adicionado é mais explícito.
                .build();
    }
}
