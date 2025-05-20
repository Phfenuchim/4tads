package com.livestock.common;

import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.user.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class GadusUserDetails implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isClient; // Flag para identificar se é cliente

    // Construtor para usuários administrativos
    public GadusUserDetails(User user) {
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
        this.isClient = false;
    }

    // Construtor para clientes
    public GadusUserDetails(Client client) {
        this.username = client.getEmail();
        this.password = client.getPassword();
        this.authorities = Collections.emptyList(); // Cliente não tem roles
        this.isClient = true;
    }

    public boolean isClient() {
        return isClient;
    }

    // Implementações necessárias dos métodos de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
