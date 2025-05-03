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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public String initialCheckout(Model model, Principal principal, HttpSession session) {

        String username = principal.getName();
        Optional<Client> client = clientRepository.findByEmail(username);

        if(client.isEmpty()){
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

        return "checkout/initialCheckout";
    }

    @PostMapping("/checkout/select-address")
    public String selectAddress(@RequestParam("addressId") UUID addressId, HttpSession session) {
        session.setAttribute("selectedAddressId", addressId);
        return "redirect:/checkout/payment";
    }

    @GetMapping("/checkout/payment")
    public String checkoutPayment(){
        return "checkout/checkout-payment";
    }

    @PostMapping("/checkout/validate")
    public String validateRequest(@RequestParam String paymentMethod,
                                @RequestParam(required = false) String cardName,
                                @RequestParam(required = false) String cardNumber,
                                @RequestParam(required = false) String cardCvv,
                                @RequestParam(required = false) String cardExpiry,
                                @RequestParam(required = false) String installments,
                                HttpSession session) {

        session.setAttribute("paymentMethod", paymentMethod);

        if ("cartao".equals(paymentMethod)) {
            if (cardName == null || cardNumber == null || cardCvv == null || cardExpiry == null || installments == null) {
                return "redirect:/checkout/payment?error=Campos do cartão obrigatórios";
            }
            session.setAttribute("cardName", cardName);
            session.setAttribute("cardNumber", cardNumber);
            session.setAttribute("cardCvv", cardCvv);
            session.setAttribute("cardExpiry",cardExpiry);
            session.setAttribute("installments", installments);
        }

        return "redirect:/checkout/confirm";
    }

    @GetMapping("/checkout/confirm")
    public String confirmRequest(Model model, HttpSession session, Principal principal) {
        String username = principal.getName();
        Optional<Client> client = clientRepository.findByEmail(username);

        if (client.isEmpty()) {
            return "redirect:/login";
        }

        List<Address> addresses = addressRepository.findByClientId(client.get().getId());
        if (addresses.isEmpty()) {
            return "redirect:/client/addresses";
        }

        UUID selectedAddressId = (UUID) session.getAttribute("selectedAddressId");

        Address address = addresses.stream()
                .filter(a -> a.getId().equals(selectedAddressId))
                .findFirst()
                .orElse(addresses.get(0)); // fallback se não encontrar

        List<CartItem> items = cartController.getCartFromSession(session);

        BigDecimal totalProdutos = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal frete = new BigDecimal("20.00"); // valor fixo de frete
        BigDecimal totalGeral = totalProdutos.add(frete);

        String paymentMethod = (String) session.getAttribute("paymentMethod");

        String installments = (String) session.getAttribute("installments");
        BigDecimal installmentValue = null;

        if ("cartao".equals(paymentMethod) && installments != null) {
            int numParcelas = Integer.parseInt(installments);
            installmentValue = totalGeral.divide(BigDecimal.valueOf(numParcelas), 2, RoundingMode.HALF_UP);
            model.addAttribute("installments", numParcelas);
            model.addAttribute("installmentValue", installmentValue);
        }

        model.addAttribute("user", client.get().getFirstName());
        model.addAttribute("address", address);
        model.addAttribute("items", items);
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("frete", frete);
        model.addAttribute("totalGeral", totalGeral);
        model.addAttribute("paymentMethod", paymentMethod);

        return "checkout/checkout-confirm";
    }

    @PostMapping("/checkout/finalizar")
    public String finalizarCompra(HttpSession session) {
        // Aqui você salva o pedido no banco ou chama o serviço de finalização

        session.removeAttribute("cart");
        session.removeAttribute("paymentMethod");
        session.removeAttribute("selectedAddressId");

        return "redirect:/checkout/sucesso";
    }
}
