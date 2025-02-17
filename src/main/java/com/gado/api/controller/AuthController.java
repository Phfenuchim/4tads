package com.gado.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/backoffice")
    public String backoffice() {
        return "backoffice";
    }
}
