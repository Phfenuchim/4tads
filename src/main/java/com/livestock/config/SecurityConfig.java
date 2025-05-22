package com.livestock.config;

import com.livestock.common.CustomAuthenticationSuccessHandler;
import com.livestock.common.GadusUserDetails;
import com.livestock.common.service.UnifiedUserDetailsService;
// Removido: import com.livestock.modules.client.services.ClientDetailsService; // Não usado
// Removido: import com.livestock.modules.user.services.CustomUserDetailsService; // Não usado
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Removido: import org.springframework.core.annotation.Order; // Não usado
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
// Removido: import org.springframework.security.web.authentication.AuthenticationSuccessHandler; // Já está no CustomAuthenticationSuccessHandler

import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Permite uso de @PreAuthorize, etc.
public class SecurityConfig {

    private final UnifiedUserDetailsService unifiedUserDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;

    // Construtor para injeção de dependências.
    public SecurityConfig(UnifiedUserDetailsService unifiedUserDetailsService,
                          CustomAuthenticationSuccessHandler successHandler) {
        this.unifiedUserDetailsService = unifiedUserDetailsService;
        this.successHandler = successHandler;
    }

// Dentro de SecurityConfig.java

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        // Permissões públicas (mantenha as suas)
                        .requestMatchers("/", "/home", "/login", "/register", "/cep/**").permitAll()
                        .requestMatchers("/products/**", "/cart/**", "/fragments/**").permitAll()
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/uploads/images/produtos/**").permitAll()
                        .requestMatchers("/frete/calcular").permitAll()
                        .requestMatchers("/api/**").permitAll()

                        // Permissões baseadas em roles (AJUSTE AQUI)
                        .requestMatchers("/admin/products/**").hasAnyRole("ADMIN", "ESTOQUISTA") // Estoquista pode gerenciar produtos
                        .requestMatchers("/admin/orders/**").hasAnyRole("ADMIN", "ESTOQUISTA") // <<-- ADICIONE ESTA LINHA ou ajuste a existente
                        // Outras rotas /admin que SÃO apenas para ADMIN devem vir DEPOIS
                        .requestMatchers("/admin/users/**").hasRole("ADMIN") // Ex: Gerenciamento de usuários SÓ para ADMIN
                        .requestMatchers("/admin/create-user").hasRole("ADMIN") // Ex: Criar usuário SÓ para ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Regra geral para /admin (cuidado, esta deve ser menos específica que as acima)

                        // Outras regras (mantenha as suas)
                        .requestMatchers("/client/**").access(this::isClient)
                        .requestMatchers("/checkout/**").authenticated()
                        .anyRequest().authenticated()
                )
                // ... resto da sua configuração (formLogin, logout, etc.) ...
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
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/cart/**"));

        return http.build();
    }


    /**
     * Regra de autorização customizada para verificar se o usuário autenticado é um cliente.
     * Um cliente é definido como um usuário autenticado que NÃO é ADMIN nem ESTOQUISTA.
     */
    private AuthorizationDecision isClient(Supplier<Authentication> authentication,
                                           RequestAuthorizationContext context) {
        Authentication auth = authentication.get();

        // Verifica se o principal é uma instância de GadusUserDetails.
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof GadusUserDetails userDetails) {
            // Verifica se o usuário NÃO tem as roles ADMIN ou ESTOQUISTA.
            boolean isAdminOrEstoquista = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ESTOQUISTA"));
            return new AuthorizationDecision(!isAdminOrEstoquista); // Concedido se NÃO for admin/estoquista
        }
        // Se não estiver autenticado ou não for do tipo esperado, nega o acesso.
        return new AuthorizationDecision(false);
    }

    /**
     * Define o provedor de autenticação que usa nosso UserDetailsService e PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(unifiedUserDetailsService); // Nosso serviço de detalhes do usuário.
        authProvider.setPasswordEncoder(passwordEncoder()); // Nosso codificador de senhas.
        return authProvider;
    }

    /**
     * Bean para o codificador de senhas (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
