package com.livestock.modules.product.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
// Removido: import com.livestock.modules.product.domain.product.Product; // Não usado diretamente como tipo de retorno ou parâmetro explícito nos métodos principais
import com.livestock.modules.product.dto.CreateProductDTO;
import com.livestock.modules.product.dto.UpdateProductDTO;
import com.livestock.modules.product.exceptions.ProductNotFoundException;
import com.livestock.modules.product.mappers.ProductMapper;
import com.livestock.modules.product.services.ProductService;
import jakarta.validation.Valid;
// Removido: import org.springframework.data.domain.Page; // Não usado diretamente
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
// Removido: import org.springframework.validation.FieldError; // Não usado diretamente
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Removido: import java.io.IOException; // Não usado diretamente
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin") // Avisar: Rota base já é /admin, então /admin/products vira /admin/admin/products. Ajustar se necessário.
public class AdminProductController {

    private final ProductService productService;
    // Avisar: 'caminhoImagens' deve ser configurável, não hardcoded. Idealmente via application.properties.
    private static final String CAMINHO_IMAGENS = "uploads/images/produtos/"; // Avisar: Nome da constante em maiúsculas.

    // Construtor para injeção de dependência.
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/create-product")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String showCreateProductForm(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles_user", userDetails.getAuthorities());
        }
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new CreateProductDTO());
        }
        return "admin/create-product";
    }

    @PostMapping("/create-product")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String createProduct(
            @Valid @ModelAttribute("product") CreateProductDTO createProductDTO,
            BindingResult bindingResult,
            @RequestParam("files") List<MultipartFile> arquivos, // Avisar: Nome do parâmetro mudado para 'arquivos' para clareza.
            @RequestParam(value = "defaultImageIndex", defaultValue = "0") int defaultImageIndex,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                model.addAttribute("email", userDetails.getUsername());
                model.addAttribute("roles_user", userDetails.getAuthorities());
            }
            return "admin/create-product"; // Retorna para a view com erros.
        }

        try {
            var product = productService.createProduct(createProductDTO);
            boolean singleImage = arquivos.stream().filter(a -> a != null && !a.isEmpty()).count() == 1; // Avisar: Adicionada verificação de nulidade para 'a'.

            for (int i = 0; i < arquivos.size(); i++) {
                MultipartFile arquivo = arquivos.get(i);
                if (arquivo != null && !arquivo.isEmpty()) { // Avisar: Adicionada verificação de nulidade para 'arquivo'.
                    String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
                    Path pasta = Paths.get(CAMINHO_IMAGENS); // Usando a constante.
                    Files.createDirectories(pasta);
                    Path caminho = pasta.resolve(nomeArquivo);
                    Files.write(caminho, arquivo.getBytes());

                    boolean isDefault = singleImage || (i == defaultImageIndex);
                    productService.addImageToProduct(product.getId(), nomeArquivo, isDefault);
                }
            }
            redirectAttributes.addFlashAttribute("message", "Produto criado com sucesso!");
            return "redirect:/admin/products"; // Avisar: Sucesso redireciona para a lista de produtos.
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao criar produto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("product", createProductDTO);
            return "redirect:/admin/create-product"; // Em caso de erro no serviço, volta para o form de criação.
        }
    }

    @GetMapping("/products/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String showEditProductForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) { // Avisar: Renomeado para clareza.
        try {
            UUID productId = UUID.fromString(id);
            if (!model.containsAttribute("product")) {
                var productEntity = productService.getProductById(productId);
                UpdateProductDTO updateProductDTO = new UpdateProductDTO(
                        productEntity.getProductName(),
                        productEntity.getActive(),
                        productEntity.getQuantity(),
                        productEntity.getDescription(),
                        productEntity.getPrice(),
                        productEntity.getRating()
                );
                model.addAttribute("product", updateProductDTO);
            }
            model.addAttribute("productId", productId);
            model.addAttribute("productImages", productService.getProductImages(productId));
            return "admin/edit-product";
        } catch (ProductNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "ID do produto inválido.");
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/products/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String updateProduct( // Avisar: Nome do método simplificado.
                                 @PathVariable("id") UUID id,
                                 @Valid @ModelAttribute("product") UpdateProductDTO updateProductDTO, // Avisar: Nome do DTO mais claro.
                                 BindingResult bindingResult,
                                 @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles, // Avisar: Nome do param.
                                 @RequestParam(value = "defaultImageId", required = false) Long defaultImageId,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        model.addAttribute("productId", id); // Garante que o ID esteja no modelo para a view.

        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("productImages", productService.getProductImages(id));
            } catch (ProductNotFoundException e) {
                redirectAttributes.addFlashAttribute("error", "Erro ao carregar dados do produto: " + e.getMessage());
                return "redirect:/admin/products";
            }
            return "admin/edit-product"; // Retorna para a view com erros.
        }

        try {
            productService.updateProduct(id, updateProductDTO);

            if (imageFiles != null) { // Avisar: Removido !imageFiles.isEmpty() pois o for já trata isso.
                for (MultipartFile file : imageFiles) {
                    if (file != null && !file.isEmpty()) {
                        byte[] bytes = file.getBytes();
                        String novoNomeArquivo = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path pasta = Paths.get(CAMINHO_IMAGENS);
                        Files.createDirectories(pasta);
                        Path caminho = pasta.resolve(novoNomeArquivo);
                        Files.write(caminho, bytes);
                        productService.addImageToProduct(id, novoNomeArquivo, false); // Avisar: Imagens adicionadas não são default por padrão.
                    }
                }
            }

            if (defaultImageId != null) {
                productService.setDefaultImage(defaultImageId, id);
            }
            redirectAttributes.addFlashAttribute("message", "Produto atualizado com sucesso!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar produto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("product", updateProductDTO);
            // Avisar: Não precisa adicionar 'id' como atributo de redirect, já está no path.
            return "redirect:/admin/products/" + id.toString() + "/edit"; // Redireciona para o GET de edição.
        }
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String listProducts(@RequestParam(required = false, defaultValue = "0") int pageNumber,
                               @RequestParam(required = false, defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String name,
                               Model model) {
        pageNumber = Math.max(0, pageNumber); // Avisar: Boa prática.

        if (name != null && !name.trim().isEmpty()) {
            var productsFilteredByName = productService.findAllProductsByNameFilterForAdmin(name);
            var productsResponseDto = productsFilteredByName.stream()
                    .map(ProductMapper::toProductResponseDTO)
                    .toList();
            model.addAttribute("products", productsResponseDto);
            model.addAttribute("isFiltered", true);
            model.addAttribute("currentFilterName", name);
        } else {
            var productsPage = productService.getAllProductsForAdminPaginated(pageNumber, pageSize);
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
        return "admin/list-products";
    }

    @GetMapping("/products/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleProductActiveStatus(@PathVariable("id") UUID id, @RequestParam("active") boolean active, RedirectAttributes redirectAttributes) {
        try {
            productService.updateProductActiveStatus(id, active);
            redirectAttributes.addFlashAttribute("message", "Status do produto atualizado com sucesso!");
        } catch (Exception e) { // Avisar: Captura genérica, pode ser mais específica.
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar status: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
