package com.livestock.modules.client.services;

import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientDetailsServiceTest {

    @Mock private ClientRepository clientRepository;

    @InjectMocks private ClientDetailsService clientDetailsService;

    @Test
    void testLoadUserByUsernameSuccess() {
        Client client = new Client();
        client.setEmail("joao@gado.com");
        client.setPassword("encodedPassword");

        when(clientRepository.findByEmail("joao@gado.com")).thenReturn(Optional.of(client));

        UserDetails userDetails = clientDetailsService.loadUserByUsername("joao@gado.com");

        assertNotNull(userDetails);
        assertEquals("joao@gado.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("CLIENT")));
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(clientRepository.findByEmail("naoexiste@gado.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> clientDetailsService.loadUserByUsername("naoexiste@gado.com"));
    }
}
