package com.gado.api.service;

import com.gado.api.domain.role.Role;
import com.gado.api.domain.user.Usr;
import com.gado.api.repositories.UsrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsrRepository usrRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usr usr = usrRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(usr.getEmail())
                .password(usr.getPassword())
                .roles(usr.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .build();
    }
}
