package com.livestock.modules.user.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.product.mappers.ProductMapper;
import com.livestock.modules.product.services.ProductService;
import com.livestock.modules.user.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private ProductService productService;

    private ProductMapper productMapper;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/home")
    public String home(Model model,
                       @RequestParam(required = false, defaultValue = "0") int pageNumber,
                       @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles", userDetails.getAuthorities());
        }

        // Carrega os produtos paginados
        var productsPage = productService.getAllProductsPaginated(pageNumber, pageSize);
        var productDTOs = productsPage.getContent().stream()
                .map(ProductMapper::toProductResponseDTO)
                .toList();

        model.addAttribute("products", productDTOs);



        model.addAttribute("pagination", PaginationResponseDTO.builder()
                .pageNumber(productsPage.getNumber())
                .pageSize(productsPage.getSize())
                .totalPages(productsPage.getTotalPages())
                .totalItems((int) productsPage.getTotalElements())
                .build());

        model.addAttribute("user", new User());
        return "home";
    }

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }
}
