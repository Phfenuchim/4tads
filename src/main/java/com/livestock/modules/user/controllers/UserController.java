package com.livestock.modules.user.controllers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    @GetMapping("/redirect")
    public String redirect() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;

            if (userDetails.getAuthorities().contains("ROLE_ADMIN")) {
                return "redirect:admin/home";
            }

            if (userDetails.getAuthorities().contains("ROLE_ESTOQUISTA")) {
                return "redirect:estoquista/home";
            }
        } return "";
    }

}
