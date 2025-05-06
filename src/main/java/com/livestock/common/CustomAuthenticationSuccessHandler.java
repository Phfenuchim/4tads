package com.livestock.common;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final SavedRequestAwareAuthenticationSuccessHandler defaultHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof GadusUserDetails userDetails) {

            if (!userDetails.isClient()) {
                // Usuários administrativos
                Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    response.sendRedirect("/admin/products");
                    return;
                } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ESTOQUISTA"))) {
                    response.sendRedirect("/admin/products");
                    return;
                } else {
                    response.sendRedirect("/home");
                    return;
                }
            }

            // Se for cliente: redireciona para URL salva ou /home
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                defaultHandler.onAuthenticationSuccess(request, response, authentication);
            } else {
                response.sendRedirect("/home");
            }

        } else {
            response.sendRedirect("/home");
        }
    }
}
