package com.livestock.modules.checkout.controllers;

import com.livestock.modules.cart.controllers.CartController;
import com.livestock.modules.cart.dto.CartItem;
import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.AddressRepository;
import com.livestock.modules.client.repositories.ClientRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class CheckeoutController {

    private final AddressRepository addressRepository;
    private final CartController cartController;
    private final ClientRepository clientRepository;

    public CheckeoutController(AddressRepository addressRepository, CartController cartController, ClientRepository clientRepository) {
        this.addressRepository = addressRepository;
        this.cartController = cartController;
        this.clientRepository = clientRepository;
    }

    @GetMapping("/checkout")
    public String inicioCheckout(Model model, Principal principal, HttpSession session) {

        String username = principal.getName();
        Optional<Client> client = clientRepository.findByEmail(username);

        if(client == null){
            return "redirect:/login";
        }

        List<Address> addresses = addressRepository.findByClientId(client.get().getId());

        if (addresses.isEmpty()) {
            return "redirect:/client/addresses";
        }

        List<CartItem> items = cartController.getCartFromSession(session);

        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", client.get().getFirstName());
        model.addAttribute("addresses", addresses);
        model.addAttribute("items", items);
        model.addAttribute("total",total);

        return "checkout";
    }
}
