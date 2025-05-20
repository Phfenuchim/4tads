package com.livestock.modules.product.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.mappers.ProductMapper;
import com.livestock.modules.product.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    // Construtor para injeção de dependência.
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(@RequestParam(required = false, defaultValue = "0") int pageNumber,
                               @RequestParam(required = false, defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String name,
                               Model model) {

        pageNumber = Math.max(0, pageNumber); // Garante que pageNumber não seja negativo.

        if (name != null && !name.trim().isEmpty()) {
            var productsFilteredByName = productService.findActiveProductsByNameFilter(name);
            var productsResponseDto = productsFilteredByName.stream()
                    .map(ProductMapper::toProductResponseDTO)
                    .toList();
            model.addAttribute("products", productsResponseDto);
            model.addAttribute("isFiltered", true); // Avisa a view que é uma busca filtrada.
            model.addAttribute("currentFilterName", name); // Para repopular o campo de busca.
        } else {
            var productsPage = productService.getAllActiveProductsPaginated(pageNumber, pageSize);
            var productsResponseDto = productsPage.getContent().stream()
                    .map(ProductMapper::toProductResponseDTO)
                    .toList();

            var pagination = PaginationResponseDTO.builder()
                    .pageNumber(productsPage.getNumber())
                    .pageSize(productsPage.getSize())
                    .totalPages(productsPage.getTotalPages())
                    .totalItems((int) productsPage.getTotalElements())
                    .build();

            model.addAttribute("products", productsResponseDto);
            model.addAttribute("pagination", pagination);
            model.addAttribute("isFiltered", false);
        }
        // O nome da view retornada é "home".
        // Verifique se este é o template correto para listar produtos.
        // Se for um template específico para produtos, poderia ser "products/list" ou similar.
        return "home";
    }

    @GetMapping("/{id}")
    public String productDetails(@PathVariable("id") UUID id, Model model) {
        Product product = productService.getProductById(id); // Lança exceção se não encontrar.
        model.addAttribute("product", ProductMapper.toProductResponseDTO(product)); // Envia DTO para a view.
        // O nome da view retornada é "fragments/product-details".
        // Verifique se este é um fragmento Thymeleaf para ser incluído ou uma página completa.
        // Se for uma página completa, o nome poderia ser "products/details".
        return "fragments/product-details";
    }
}
