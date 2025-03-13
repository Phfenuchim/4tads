package com.livestock.modules.product.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.dto.CreateProductDTO;
import com.livestock.modules.product.dto.UpdateProductDTO;
import com.livestock.modules.product.exceptions.ProductNotFoundException;
import com.livestock.modules.product.mappers.ProductMapper;
import com.livestock.modules.product.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/create-product")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String showCreateProductForm(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles_user", userDetails.getAuthorities());
        }

        model.addAttribute("product", new CreateProductDTO());
        return "admin/create-product";
    }

    @PostMapping("/create-product")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String createProduct(@ModelAttribute("product") CreateProductDTO createProductDTO, RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(createProductDTO);
            redirectAttributes.addFlashAttribute("message", "Produto criado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/create-product";
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String toggleProductActiveStatus(@PathVariable("id") String id, @RequestParam(required = true) boolean active, RedirectAttributes redirectAttributes) {
        try {
            productService.updateProductActiveStatus(UUID.fromString(id), active);
            redirectAttributes.addFlashAttribute("message", active ? "Produto ativado com sucesso!" : "Produto desativado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products";
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String editProductById(@PathVariable("id") String id, Model model) {
        try {
            var product = productService.getProductById(UUID.fromString(id));
            model.addAttribute("product", product);
            model.addAttribute("productImages", productService.getProductImages(UUID.fromString(id)));
            return "edit-product";
        } catch (ProductNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/products/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String updateProduct(
            @PathVariable("id") UUID id,
            @ModelAttribute("product") UpdateProductDTO updateProductDTO,
            RedirectAttributes redirectAttributes) {

        try {
            productService.updateProduct(id, updateProductDTO);
            redirectAttributes.addFlashAttribute("message", "Produto atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/add-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String addProductImage(
            @PathVariable("id") UUID id,
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "isDefault", required = false, defaultValue = "false") boolean isDefault,
            RedirectAttributes redirectAttributes) {

        try {
            // Aqui você precisaria implementar a lógica para salvar o arquivo
            // e obter a URL do caminho onde ele foi salvo
            String pathUrl = saveImageAndGetPath(file);

            productService.addImageToProduct(id, pathUrl, isDefault);
            redirectAttributes.addFlashAttribute("message", "Imagem adicionada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products/" + id + "/edit";
    }

    @GetMapping("/products/{productId}/images/{imageId}/set-default")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String setDefaultImage(
            @PathVariable("productId") UUID productId,
            @PathVariable("imageId") Long imageId,
            RedirectAttributes redirectAttributes) {

        try {
            productService.setDefaultImage(imageId, productId);
            redirectAttributes.addFlashAttribute("message", "Imagem padrão definida com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/edit";
    }

    @GetMapping("/products/{productId}/images/{imageId}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String deleteProductImage(
            @PathVariable("productId") UUID productId,
            @PathVariable("imageId") Long imageId,
            RedirectAttributes redirectAttributes) {

        try {
            productService.deleteProductImage(imageId);
            redirectAttributes.addFlashAttribute("message", "Imagem removida com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products/" + productId + "/edit";
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
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

        return "admin/list-products";
    }


    @GetMapping("/products/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Produto excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // Método auxiliar para salvar a imagem e retornar o caminho
    private String saveImageAndGetPath(MultipartFile file) throws IOException {
        // Implementação para salvar o arquivo e retornar o caminho
        // Esta é uma implementação de exemplo, você precisará adaptá-la ao seu sistema de armazenamento

        // Exemplo simples (não use em produção sem adaptações):
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String uploadDir = "uploads/products/";
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }

        java.nio.file.Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        return "/uploads/products/" + fileName;
    }
}
