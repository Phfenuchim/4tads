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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        // Permissões públicas
                        .requestMatchers("/", "/home", "/login", "/register", "/cep/**").permitAll()
                        .requestMatchers("/products/**", "/cart/**", "/fragments/**").permitAll()
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/uploads/images/produtos/**").permitAll() // Adicionado /uploads/images/produtos/** para consistência
                        .requestMatchers("/frete/calcular").permitAll()
                        .requestMatchers("/api/**").permitAll() // APIs públicas
                        // Permissões baseadas em roles
                        .requestMatchers("/admin/products/**").hasAnyRole("ADMIN", "ESTOQUISTA") // Produtos admin/estoquista
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Outras rotas admin
                        .requestMatchers("/client/**").access(this::isClient) // Acesso específico para clientes
                        .requestMatchers("/checkout/**").authenticated() // Checkout requer autenticação
                        .anyRequest().authenticated() // Qualquer outra requisição requer autenticação
                )
                .formLogin(form -> form
                        .loginPage("/login") // Página de login customizada
                        .successHandler(successHandler) // Handler para sucesso no login
                        .permitAll() // Permitir acesso à página de login
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL para fazer logout
                        .logoutSuccessUrl("/home") // Redireciona para home após logout
                        .invalidateHttpSession(true) // Invalida a sessão
                        .deleteCookies("JSESSIONID") // Remove o cookie de sessão
                        .permitAll() // Permitir acesso à funcionalidade de logout
                );

        // Define o provedor de autenticação customizado.
        http.authenticationProvider(authenticationProvider());
        // Desabilita CSRF para rotas /api/** (comum para APIs stateless).
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/cart/**")); // Adicionado /cart/** se as operações de carrinho são via API/AJAX

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
