package com.livestock.modules.user.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.product.mappers.ProductMapper;
import com.livestock.modules.product.services.ProductService;
// Removido: import com.livestock.modules.user.domain.user.User; // Apenas 'new User()' era usado, pode ser desnecessário.
// Removido: import jakarta.servlet.http.HttpServletRequest; // Não usado
// Removido: import jakarta.servlet.http.HttpServletResponse; // Não usado
import com.livestock.modules.user.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
// Removido: import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler; // Não usado
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// Removido: import org.springframework.web.bind.annotation.PostMapping; // Não usado
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final ProductService productService;
    // Avisar: ProductMapper não estava sendo inicializado nem injetado.
    // Se ProductMapper.toProductResponseDTO é um método estático, a instância não é necessária.
    // Se não for estático, ProductMapper precisaria ser injetado ou instanciado.
    // private final ProductMapper productMapper; // Removido se toProductResponseDTO for estático.

    // Construtor para injeção de dependência.
    public AuthController(ProductService productService /*, ProductMapper productMapper // Adicionar se não for estático */) {
        this.productService = productService;
        // this.productMapper = productMapper; // Adicionar se não for estático
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model,
                       @RequestParam(required = false, defaultValue = "0") int pageNumber,
                       @RequestParam(required = false, defaultValue = "10") int pageSize) {

        pageNumber = Math.max(0, pageNumber); // Avisar: Boa prática.

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) { // Avisar: Adicionada verificação de nulidade para 'authentication'.
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles", userDetails.getAuthorities());
        }

        var productsPage = productService.getAllActiveProductsPaginated(pageNumber, pageSize);
        var productDTOs = productsPage.getContent().stream()
                .map(ProductMapper::toProductResponseDTO) // Assumindo que toProductResponseDTO é estático.
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
