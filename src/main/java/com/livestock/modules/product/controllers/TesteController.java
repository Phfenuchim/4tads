package com.livestock.modules.product.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TesteController {

    @GetMapping("/dropdown")
    public String testeDropdown() {
        return "dropdown";
    }
}

