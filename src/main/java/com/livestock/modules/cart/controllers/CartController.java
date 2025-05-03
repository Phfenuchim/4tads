package com.livestock.modules.cart.controllers;

import com.livestock.modules.cart.dto.CartItem;
import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.domain.product_image.Product_image;
import com.livestock.modules.product.services.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);

        BigDecimal total = cart.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "view-cart";
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam UUID productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            List<CartItem> cart = getCartFromSession(session);
            Product product = productService.getProductById(productId);

            // Verifica se o produto já está no carrinho
            Optional<CartItem> existingItem = cart.stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Atualiza a quantidade se o produto já estiver no carrinho
                existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            } else {
                // Adiciona novo item ao carrinho
                CartItem newItem = new CartItem();
                newItem.setProductId(productId);
                newItem.setProductName(product.getProductName());
                newItem.setPrice(product.getPrice());
                newItem.setQuantity(quantity);

                // Obtém a imagem padrão do produto, se existir
                var productImages = productService.getProductImages(productId);


                // Extrai a URL da imagem padrão ou usa um fallback
                String imageUrl = productImages.stream()
                        .filter(img -> Boolean.TRUE.equals(img.getDefaultImage()))
                        .map(Product_image::getPathUrl)
                        .findFirst()
                        .orElse("logo.png");

                newItem.setImageUrl(imageUrl);
                cart.add(newItem);
            }

            session.setAttribute("cart", cart);
            redirectAttributes.addFlashAttribute("message", "Produto adicionado ao carrinho!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao adicionar produto ao carrinho: " + e.getMessage());
        }

        return "redirect:/products";
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

    public List<CartItem> getCartFromSession(HttpSession session) {
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
                });

        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

}
