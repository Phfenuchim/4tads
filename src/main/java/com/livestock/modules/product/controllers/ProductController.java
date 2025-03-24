package com.livestock.modules.product.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.mappers.ProductMapper;
import com.livestock.modules.product.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String listProducts(@RequestParam(required = false, defaultValue = "0") int pageNumber,
                               @RequestParam(required = false, defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String name,
                               Model model) {
        if (name != null && !name.trim().isEmpty()) {
            var productsFilteredByName = productService.findAllProductsByNameFilter(name);
            var productsResponseDto = productsFilteredByName.stream()
                    .map(ProductMapper::toProductResponseDTO)
                    .toList();
            model.addAttribute("products", productsResponseDto);
        } else {
            var productsPage = productService.getAllProductsPaginated(pageNumber, pageSize);
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
        }

        return "home";  // Alterado para apontar para o template correto
    }

    @GetMapping("/{id}")
    public String productDetails(@PathVariable("id") UUID id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "view-product-details";
    }
}
