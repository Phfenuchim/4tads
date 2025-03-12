package com.livestock.modules.user.controllers;

import com.livestock.modules.user.domain.user.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal=authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles", userDetails.getAuthorities());
        }

        model.addAttribute("user", new User());
        return "home";
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }
}
