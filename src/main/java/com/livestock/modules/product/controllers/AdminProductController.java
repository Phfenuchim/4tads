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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    @Autowired
    private ProductService productService;
    private static String caminhoImagens = "src/main/resources/static/images/produtos/";

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
    public String createProduct(
            @ModelAttribute("product") CreateProductDTO createProductDTO,
            RedirectAttributes redirectAttributes,
            @RequestParam(value = "files") List<MultipartFile> arquivos) {

        try {
            var product = productService.createProduct(createProductDTO);

            int index = 0;
            for (MultipartFile arquivo : arquivos) {
                byte[] bytes = arquivo.getBytes();
                String novoNomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
                Path caminho = Paths.get(caminhoImagens + novoNomeArquivo);
                Files.write(caminho, bytes);

                // Define a primeira imagem como padrão (index == 0)
                boolean isDefault = (index == 0);
                productService.addImageToProduct(product.getId(), novoNomeArquivo, isDefault);
                index++;
            }

            redirectAttributes.addFlashAttribute("message", "Produto criado com sucesso!");
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/create-product";
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
            return "admin/edit-product";
        } catch (ProductNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/products/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String updateProductAndDefaultImage(
            @PathVariable("id") UUID id,
            @ModelAttribute("product") UpdateProductDTO updateProductDTO,
            @RequestParam(value = "imageFile", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "defaultImageId", required = false) Long defaultImageId,
            RedirectAttributes redirectAttributes) {

        try {
            // Atualiza os dados do produto
            productService.updateProduct(id, updateProductDTO);

            // Processa o upload de novas imagens, se houver
            if (imageFiles != null && !imageFiles.isEmpty()) {
                int index = 0;
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        byte[] bytes = file.getBytes();
                        String novoNomeArquivo = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path caminho = Paths.get(caminhoImagens + novoNomeArquivo);
                        Files.write(caminho, bytes);

                        boolean isDefault = (index == 0); // Primeira imagem como padrão
                        productService.addImageToProduct(id, novoNomeArquivo, isDefault);
                        index++;
                    }
                }
            }

            // Define a imagem padrão, se o parâmetro defaultImageId for enviado
            if (defaultImageId != null) {
                productService.setDefaultImage(defaultImageId, id);
                redirectAttributes.addFlashAttribute("message", "Imagem padrão atualizada com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("message", "Produto atualizado com sucesso!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products/" + id + "/edit";
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


}
