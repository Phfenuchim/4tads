package com.livestock.modules.user.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/backoffice")
    @PreAuthorize("hasAnyRole('ESTOQUISTA', 'ADMIN')")
    public String backoffice() {
        return "backoffice";
    }

    @GetMapping("/home")
    @PreAuthorize("hasAnyRole('ESTOQUISTA', 'ADMIN')")
    public String home() {return "home";}

}
