package com.livestock.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (authentication.getPrincipal() instanceof GadusUserDetails) {
            GadusUserDetails userDetails = (GadusUserDetails) authentication.getPrincipal();

            if (userDetails.isClient()) {
                // É um cliente
                response.sendRedirect("/home");
            } else {
                // É um usuário administrativo
                Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    response.sendRedirect("/admin/products");
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ESTOQUISTA"))) {
                    response.sendRedirect("/admin/products");
                } else {
                    response.sendRedirect("/home");
                }
            }
        } else {
            // Fallback
            response.sendRedirect("/home");
        }
    }
}
