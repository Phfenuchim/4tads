package com.livestock.modules.user.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/backoffice")
    @PreAuthorize("hasAnyRole('ESTOQUISTA', 'CLIENTE')")
    public String backoffice() {
        return "backoffice";
    }

    @GetMapping("/home")
    public String home() {return "home";}

}
