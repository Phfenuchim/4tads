package com.livestock.modules.checkout.controllers;

import com.livestock.modules.cart.controllers.CartController;
import com.livestock.modules.cart.dto.CartItem;
import com.livestock.modules.checkout.domain.Freight;
import com.livestock.modules.checkout.services.CheckoutService;
import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.AddressRepository;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.domain.order.Order;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.*;

@Controller
public class CheckoutController {

    private final AddressRepository addressRepository;
    private final CartController cartController;
    private final ClientRepository clientRepository;
    private final CheckoutService checkoutService;

    public CheckoutController(AddressRepository addressRepository, CartController cartController, ClientRepository clientRepository, CheckoutService checkoutService) {
        this.addressRepository = addressRepository;
        this.cartController = cartController;
        this.clientRepository = clientRepository;
        this.checkoutService = checkoutService;
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

        return "checkout/initial-checkout";
    }

    @PostMapping("/checkout/select-address")
    public String selectAddress(@RequestParam("addressId") UUID addressId, HttpSession session) {
        session.setAttribute("selectedAddressId", addressId);
        return "redirect:/checkout/payment";
    }

    @GetMapping("/checkout/payment")
    public String checkoutPayment(Model model) {
        model.addAttribute("freteTipos", Freight.values());
        return "checkout/checkout-payment";
    }

    @PostMapping("/checkout/validate")
    public String validateRequest(@RequestParam String paymentMethod,
                                  @RequestParam String freteTipo,
                                  @RequestParam Map<String, String> allParams,
                                  HttpSession session) {

        // Armazenar parâmetros na sessão
        session.setAttribute("paymentMethod", paymentMethod);
        session.setAttribute("freteTipo", freteTipo);

        // Para cartão, validar campos específicos
        if ("cartao".equals(paymentMethod)) {
            Map<String, String> cardDetails = new HashMap<>();
            cardDetails.put("cardName", allParams.get("cardName"));
            cardDetails.put("cardNumber", allParams.get("cardNumber"));
            cardDetails.put("cardCvv", allParams.get("cardCvv"));
            cardDetails.put("cardExpiry", allParams.get("cardExpiry"));
            cardDetails.put("installments", allParams.get("installments"));

            try {
                // Validar com a estratégia
                boolean valid = checkoutService.validatePaymentDetails(paymentMethod, cardDetails);
                if (!valid) {
                    return "redirect:/checkout/payment?error=Dados do cartão inválidos";
                }

                // Armazenar detalhes do cartão
                for (Map.Entry<String, String> entry : cardDetails.entrySet()) {
                    session.setAttribute(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                return "redirect:/checkout/payment?error=" + e.getMessage();
            }
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


        String selectedFreight = (String) session.getAttribute("freteTipo");
        Freight freteTipo = Freight.valueOf(selectedFreight);
        BigDecimal freight = freteTipo.getValor();

        BigDecimal totalGeral = totalProdutos.add(freight);

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
        model.addAttribute("frete", freight);
        model.addAttribute("freteTipo", freteTipo.getNome());
        model.addAttribute("totalGeral", totalGeral);
        model.addAttribute("paymentMethod", paymentMethod);

        return "checkout/checkout-confirm";
    }

    @PostMapping("/checkout/finalizar")
    public String finalizarCompra(HttpSession session, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            // Chamar o serviço para criar o pedido
            Order order = checkoutService.createOrder(principal, session);

            // Limpar a sessão
            session.removeAttribute("cart");
            session.removeAttribute("paymentMethod");
            session.removeAttribute("selectedAddressId");
            session.removeAttribute("freteTipo");

            // Adicionar informações para a página de sucesso
            redirectAttributes.addFlashAttribute("orderNumber", order.getOrderNumber());
            redirectAttributes.addFlashAttribute("orderTotal", order.getTotalPrice());
            redirectAttributes.addFlashAttribute("success", true);

            return "redirect:/checkout/checkout-success";
        } catch (Exception e) {
            // Em caso de erro, redirecionar com mensagem de erro
            redirectAttributes.addFlashAttribute("error", "Erro ao finalizar compra: " + e.getMessage());
            return "redirect:/checkout/checkout-confirm";
        }
    }

    @GetMapping("/checkout/checkout-success")
    public String checkoutSucesso(Model model) {
        if (!model.containsAttribute("orderNumber")) {
            // Se acessou diretamente sem passar pelo fluxo de checkout
            return "redirect:/home";
        }
        return "checkout/checkout-success";
    }

}
