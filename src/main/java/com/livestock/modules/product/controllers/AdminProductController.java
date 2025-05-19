package com.livestock.modules.product.controllers;

import com.livestock.common.dto.PaginationResponseDTO;
import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.dto.CreateProductDTO;
import com.livestock.modules.product.dto.UpdateProductDTO;
import com.livestock.modules.product.exceptions.ProductNotFoundException;
import com.livestock.modules.product.mappers.ProductMapper;
import com.livestock.modules.product.services.ProductService;
import jakarta.validation.Valid; // Importar @Valid
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Importar BindingResult
import org.springframework.validation.FieldError;
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
    private static final String caminhoImagens = "uploads/images/produtos/";

    @GetMapping("/create-product")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String showCreateProductForm(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            model.addAttribute("email", userDetails.getUsername());
            model.addAttribute("roles_user", userDetails.getAuthorities());
        }

        // Adiciona um DTO vazio ao modelo para o formulário Thymeleaf
        if (!model.containsAttribute("product")) { // Evita sobrescrever se já houver um com erros
            model.addAttribute("product", new CreateProductDTO());
        }
        return "admin/create-product";
    }

    @PostMapping("/create-product")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String createProduct(
            @Valid @ModelAttribute("product") CreateProductDTO createProductDTO, // Adicionado @Valid
            BindingResult bindingResult, // Adicionado BindingResult
            RedirectAttributes redirectAttributes,
            @RequestParam("files") List<MultipartFile> arquivos,
            @RequestParam(value = "defaultImageIndex", defaultValue = "0") int defaultImageIndex,
            Model model) { // Adicionado Model para retornar ao formulário com erros

        if (bindingResult.hasErrors()) {
            // Se houver erros de validação, adiciona os atributos necessários de volta ao modelo
            // e retorna para o formulário de criação para exibir os erros.
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                model.addAttribute("email", userDetails.getUsername());
                model.addAttribute("roles_user", userDetails.getAuthorities());
            }
            // O 'product' com os erros já está no BindingResult e será usado pelo Thymeleaf
            // Não precisa adicionar `redirectAttributes` aqui, pois estamos renderizando a view diretamente.
            return "admin/create-product";
        }

        try {
            var product = productService.createProduct(createProductDTO);

            boolean singleImage = arquivos.stream().filter(a -> !a.isEmpty()).count() == 1;

            for (int i = 0; i < arquivos.size(); i++) {
                MultipartFile arquivo = arquivos.get(i);
                if (!arquivo.isEmpty()) {
                    String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
                    Path pasta = Paths.get(caminhoImagens);
                    Files.createDirectories(pasta);
                    Path caminho = pasta.resolve(nomeArquivo);
                    Files.write(caminho, arquivo.getBytes());

                    boolean isDefault = singleImage || i == defaultImageIndex;
                    productService.addImageToProduct(product.getId(), nomeArquivo, isDefault);
                }
            }
            redirectAttributes.addFlashAttribute("message", "Produto criado com sucesso!");
        } catch (Exception e) {
            // Para outros erros (não validação), pode-se adicionar o DTO de volta
            // aos redirectAttributes para repopular o formulário se necessário,
            // mas é mais comum exibir uma mensagem de erro genérica aqui.
            redirectAttributes.addFlashAttribute("error", "Erro ao criar produto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("product", createProductDTO); // Repopula o formulário
            return "redirect:/admin/create-product";
        }
        return "redirect:/admin/products";
    }


    @GetMapping("/products/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String editProductById(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UUID productId = UUID.fromString(id);
            // Se não houver um 'product' já no model (vindo de um erro de update), busca do serviço
            if (!model.containsAttribute("product")) {
                var productEntity = productService.getProductById(productId);
                // Mapear a entidade para o DTO de update para popular o formulário
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
            model.addAttribute("productId", productId); // Passar o ID para o formulário
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
    public String updateProductAndDefaultImage(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute("product") UpdateProductDTO submittedUpdateProductDTO, // "product" é o nome do objeto no model
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "defaultImageId", required = false) Long defaultImageId,
            RedirectAttributes redirectAttributes, // Usado para redirecionamentos
            Model model) { // Usado para renderizar a view diretamente

        // Adiciona o productId ao modelo independentemente de haver erros ou não,
        // pois a action do formulário precisa dele.
        model.addAttribute("productId", id);

        if (bindingResult.hasErrors()) {
            // O objeto 'product' (submittedUpdateProductDTO) já está no modelo
            // implicitamente devido ao @ModelAttribute("product") e ao BindingResult.
            // O Thymeleaf usará este objeto para repopular os campos e mostrar os erros.

            // Precisamos carregar as imagens novamente para exibir na página de edição
            try {
                model.addAttribute("productImages", productService.getProductImages(id));
            } catch (ProductNotFoundException e) {
                // Se o produto não for encontrado aqui, é um problema mais sério.
                // Pode ser melhor redirecionar para a lista com uma mensagem de erro geral.
                redirectAttributes.addFlashAttribute("error", "Erro ao carregar dados do produto para edição: " + e.getMessage());
                return "redirect:/admin/products";
            }
            // Retorna para a view de edição para mostrar os erros e os dados que o usuário inseriu.
            return "admin/edit-product";
        }

        // Se não houver erros de validação, prossiga com a lógica de atualização
        try {
            productService.updateProduct(id, submittedUpdateProductDTO);

            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile file : imageFiles) {
                    if (file != null && !file.isEmpty()) {
                        byte[] bytes = file.getBytes();
                        String novoNomeArquivo = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        Path pasta = Paths.get(caminhoImagens);
                        Files.createDirectories(pasta);
                        Path caminho = pasta.resolve(novoNomeArquivo);
                        Files.write(caminho, bytes);
                        productService.addImageToProduct(id, novoNomeArquivo, false);
                    }
                }
            }

            if (defaultImageId != null) {
                productService.setDefaultImage(defaultImageId, id);
                redirectAttributes.addFlashAttribute("message", "Produto e imagem padrão atualizados com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("message", "Produto atualizado com sucesso!");
            }

        } catch (Exception e) {
            // Erros que não são de validação (ex: erro de banco de dados)
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar produto: " + e.getMessage());
            // Adiciona o DTO submetido de volta para repopular o formulário em caso de erro NO SERVIÇO
            // Isso ajuda o usuário a não perder o que digitou.
            redirectAttributes.addFlashAttribute("product", submittedUpdateProductDTO);
            // Também precisamos passar o productId para o redirect
            redirectAttributes.addAttribute("id", id);
            return "redirect:/admin/products/{id}/edit"; // Redireciona para o GET de edição
        }

        return "redirect:/admin/products"; // Sucesso, redireciona para a lista
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTOQUISTA')")
    public String listProducts(@RequestParam(required = false, defaultValue = "0") int pageNumber,
                               @RequestParam(required = false, defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String name,
                               Model model) {
        // Lógica de listagem existente
        if (name != null && !name.trim().isEmpty()) {
            var productsFilteredByName = productService.findAllProductsByNameFilterForAdmin(name);
            var productsResponseDto = productsFilteredByName.stream()
                    .map(ProductMapper::toProductResponseDTO)
                    .toList();
            model.addAttribute("products", productsResponseDto);
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
        }

        return "admin/list-products";
    }


    @GetMapping("/products/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleProductActiveStatus(@PathVariable("id") UUID id, @RequestParam("active") boolean active, RedirectAttributes redirectAttributes) {
        try {
            productService.updateProductActiveStatus(id, active);
            redirectAttributes.addFlashAttribute("message", "Status do produto atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar status do produto: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
