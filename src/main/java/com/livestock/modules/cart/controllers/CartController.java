package com.livestock.modules.cart.controllers;

import com.livestock.modules.cart.adapter.CartAdapter;
import com.livestock.modules.cart.composite.CartCompositeGroup;
import com.livestock.modules.cart.dto.CartItem;
import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.domain.product_image.Product_image;
import com.livestock.modules.product.services.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final ProductService productService;

    // Construtor para injeção de dependência
    public CartController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        CartCompositeGroup cartComposite = CartAdapter.toCompositeGroup(cart);
        BigDecimal total = cartComposite.getTotalPrice();

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "view-cart"; // Supondo que o nome da view seja "view-cart.html"
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam UUID productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            List<CartItem> cart = getCartFromSession(session);
            Product product = productService.getProductById(productId); // Lança exceção se não encontrar

            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            } else {
                CartItem newItem = new CartItem();
                newItem.setProductId(productId);
                newItem.setProductName(product.getProductName()); // Usar o nome do produto da entidade
                newItem.setPrice(product.getPrice());             // Usar o preço da entidade
                newItem.setQuantity(quantity);

                List<Product_image> productImages = productService.getProductImages(productId);
                String imageUrl = productImages.stream()
                        .filter(img -> Boolean.TRUE.equals(img.getDefaultImage()))
                        .map(Product_image::getPathUrl)
                        .findFirst()
                        .orElse("logo.png"); // Imagem padrão se nenhuma for default
                newItem.setImageUrl(imageUrl);
                cart.add(newItem);
            }

            session.setAttribute("cart", cart);
            redirectAttributes.addFlashAttribute("message", "Produto adicionado ao carrinho!");

        } catch (Exception e) {
            // É uma boa prática logar a exceção no servidor
            // logger.error("Erro ao adicionar produto ao carrinho: {}", productId, e);
            redirectAttributes.addFlashAttribute("error", "Erro ao adicionar produto ao carrinho: " + e.getMessage());
        }

        // Redirecionar para a página anterior ou para uma página específica de produtos
        // Se você quiser voltar para a página de detalhes do produto, precisará do ID do produto no redirect.
        // Ou pode ser um redirect fixo, como para a lista de produtos.
        return "redirect:/products"; // Ajuste conforme necessário
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam UUID productId, HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        cart.removeIf(item -> item.getProductId().equals(productId));
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute("cart");
        return "redirect:/cart";
    }

    // Este método é público e pode ser chamado por outros serviços, como CheckoutService
    public List<CartItem> getCartFromSession(HttpSession session) {
        @SuppressWarnings("unchecked") // Suprime o warning de cast não verificado
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @PostMapping("/increase")
    public String increaseQuantity(@RequestParam UUID productId, HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        cart.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(item.getQuantity() + 1));
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/decrease")
    public String decreaseQuantity(@RequestParam UUID productId, HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        cart.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                    }
                    // Se quiser remover o item se a quantidade for 0, adicione a lógica aqui
                    // else { cart.remove(item); } // Cuidado com ConcurrentModificationException se não iterar corretamente
                });
        // Se a quantidade for 1 e o usuário diminuir, o item deve ser removido?
        // Se sim, a lógica acima precisaria de ajuste ou um novo botão "remover item".
        // Para evitar ConcurrentModificationException ao remover durante a iteração, use removeIf
        // Ex: cart.removeIf(item -> item.getProductId().equals(productId) && item.getQuantity() <= 0);
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @GetMapping("/verify-checkout")
    public String verifyCheckout(Principal principal, HttpSession session) {
        if (principal == null) {
            session.setAttribute("redirectAfterLogin", "/cart"); // Redireciona para o carrinho após login
            return "redirect:/login";
        }
        return "redirect:/checkout";
    }
}
