package com.livestock.config;

import com.livestock.common.CustomAuthenticationSuccessHandler;
import com.livestock.common.GadusUserDetails;
import com.livestock.common.service.UnifiedUserDetailsService;
import com.livestock.modules.client.services.ClientDetailsService;
import com.livestock.modules.user.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UnifiedUserDetailsService unifiedUserDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/", "/home", "/login", "/register", "/cep/**").permitAll()
                        .requestMatchers("/products/**", "/cart/**", "/fragments/**").permitAll()
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                        .requestMatchers("/frete/calcular").permitAll()
                        .requestMatchers("/admin/products/**").hasAnyRole("ADMIN", "ESTOQUISTA")
                        .requestMatchers("/admin/*").hasRole("ADMIN")
                        .requestMatchers("/client/**").access(this::isClient) // Usa método personalizado
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    // Método para verificar se é cliente
    private AuthorizationDecision isClient(Supplier<Authentication> authentication,
                                           RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth.getPrincipal() instanceof GadusUserDetails) {
            return new AuthorizationDecision(((GadusUserDetails) auth.getPrincipal()).isClient());
        }
        return new AuthorizationDecision(false);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(unifiedUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

