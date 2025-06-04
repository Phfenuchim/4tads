package com.livestock.modules.checkout.controllers;

import com.livestock.modules.cart.controllers.CartController;
import com.livestock.modules.cart.dto.CartItem;
import com.livestock.modules.checkout.domain.Freight;
import com.livestock.modules.checkout.services.CheckoutService;
import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.address.AddressType;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.AddressRepository;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
import com.livestock.modules.order.infra.apis.FreteResponse;
import com.livestock.modules.order.infra.apis.OpcaoFrete; // Certifique-se de importar OpcaoFrete
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
import java.util.stream.Collectors;

@Controller
public class CheckoutController {

    private final AddressRepository addressRepository;
    private final CartController cartController;
    private final ClientRepository clientRepository;
    private final CheckoutService checkoutService;
    private final ConsultaCepAPI consultaCepAPI;

    public CheckoutController(AddressRepository addressRepository,
                              CartController cartController,
                              ClientRepository clientRepository,
                              CheckoutService checkoutService,
                              ConsultaCepAPI consultaCepAPI) {
        this.addressRepository = addressRepository;
        this.cartController = cartController;
        this.clientRepository = clientRepository;
        this.checkoutService = checkoutService;
        this.consultaCepAPI = consultaCepAPI;
    }

    @GetMapping("/checkout")
    public String initialCheckout(Model model, Principal principal, HttpSession session) {
        String username = principal.getName();
        Optional<Client> clientOpt = clientRepository.findByEmail(username);

        if (clientOpt.isEmpty()) {
            return "redirect:/login";
        }
        Client client = clientOpt.get();

        // 1. Buscar todos os endereços do cliente
        List<Address> allClientAddresses = addressRepository.findByClientId(client.getId());

        // 2. Filtrar para manter apenas os endereços de ENTREGA
        List<Address> deliveryAddresses = allClientAddresses.stream()
                .filter(address -> AddressType.ENTREGA.equals(address.getType())) // Filtra pelo tipo ENTREGA
                .collect(Collectors.toList());

        List<CartItem> items = cartController.getCartFromSession(session);
        if (items == null || items.isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("userFirstName", client.getFirstName());
        model.addAttribute("addresses", deliveryAddresses); // Passar a lista filtrada para o modelo
        model.addAttribute("items", items);
        model.addAttribute("subtotal", total);

        return "checkout/initial-checkout";
    }


    @PostMapping("/checkout/select-address")
    public String selectAddress(@RequestParam("addressId") UUID addressId, HttpSession session) {
        session.setAttribute("selectedAddressId", addressId);
        return "redirect:/checkout/payment";
    }

    @GetMapping("/checkout/payment")
    public String checkoutPayment(Model model, HttpSession session) {
        UUID selectedAddressId = (UUID) session.getAttribute("selectedAddressId");

        if (selectedAddressId == null) {
            return "redirect:/checkout";
        }

        Address address = addressRepository.findById(selectedAddressId)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado para o ID: " + selectedAddressId));

        // CORREÇÃO: Usar consultaCepViaApiInterna para obter o FreteResponse com valores calculados.
        FreteResponse freteResponse = consultaCepAPI.consultaCepViaApiInterna(address.getCep());

        if (freteResponse == null || freteResponse.getOpcoesFrete() == null || freteResponse.getOpcoesFrete().isEmpty()) {
            model.addAttribute("freteError", "Não foi possível calcular o frete para este endereço.");
            model.addAttribute("freteOpcoes", Collections.emptyList());
        } else {
            model.addAttribute("freteOpcoes", freteResponse.getOpcoesFrete());
            session.setAttribute("freteOpcoesDisponiveis", freteResponse.getOpcoesFrete());
        }

        return "checkout/checkout-payment";
    }

    @PostMapping("/checkout/validate")
    public String validateRequest(
            @RequestParam String paymentMethod,
            @RequestParam String freteTipo, // Ex: "GADO_RAPIDO"
            @RequestParam Map<String, String> allParams,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        session.setAttribute("paymentMethod", paymentMethod.toLowerCase());
        session.setAttribute("freteTipo", freteTipo); // Armazena o ENUM_NAME do frete escolhido

        UUID addressId = (UUID) session.getAttribute("selectedAddressId");
        if (addressId == null) {
            return "redirect:/checkout";
        }
        // Não precisamos do 'Address address' aqui, pois o valor do frete virá da sessão.

        // Recupera as opções de frete (com valores já calculados) da sessão.
        List<OpcaoFrete> freteOpcoesDisponiveis = (List<OpcaoFrete>) session.getAttribute("freteOpcoesDisponiveis");

        if (freteOpcoesDisponiveis == null || freteOpcoesDisponiveis.isEmpty()) {
            redirectAttributes.addFlashAttribute("paymentError", "Erro ao carregar opções de frete. Tente novamente.");
            return "redirect:/checkout/payment";
        }

        // Encontra a opção de frete selecionada pelo usuário e obtém seu valor.
        Optional<OpcaoFrete> opcaoFreteSelecionadaOpt = freteOpcoesDisponiveis.stream()
                .filter(opcao -> opcao.getEnumName().equals(freteTipo))
                .findFirst();

        if (opcaoFreteSelecionadaOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("paymentError", "Tipo de frete selecionado é inválido.");
            return "redirect:/checkout/payment";
        }

        OpcaoFrete opcaoFreteSelecionada = opcaoFreteSelecionadaOpt.get();
        BigDecimal valorFrete = opcaoFreteSelecionada.getValor(); // Pega o valor JÁ CALCULADO da opção.
        session.setAttribute("valorFrete", valorFrete); // Armazena o VALOR do frete na sessão.

        // Se o método for cartão, coleta e valida os detalhes do cartão.
        if ("cartao".equalsIgnoreCase(paymentMethod)) {
            Map<String, String> cardDetails = new HashMap<>();
            cardDetails.put("cardName", allParams.get("cardName"));
            cardDetails.put("cardNumber", allParams.get("cardNumber"));
            cardDetails.put("cardCvv", allParams.get("cardCvv"));
            cardDetails.put("cardExpiry", allParams.get("cardExpiry"));
            cardDetails.put("installments", allParams.get("installments"));

            try {
                boolean valid = checkoutService.validatePaymentDetails(paymentMethod.toLowerCase(), cardDetails);
                if (!valid) {
                    redirectAttributes.addFlashAttribute("paymentError", "Dados do cartão inválidos. Verifique as informações.");
                    return "redirect:/checkout/payment";
                }
                session.setAttribute("cardDetails", cardDetails);

            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("paymentError", e.getMessage());
                return "redirect:/checkout/payment";
            }
        }

        return "redirect:/checkout/confirm";
    }

    @GetMapping("/checkout/confirm")
    public String confirmRequest(Model model, HttpSession session, Principal principal) {
        String username = principal.getName();
        Optional<Client> clientOpt = clientRepository.findByEmail(username);

        if (clientOpt.isEmpty()) {
            return "redirect:/login";
        }
        Client client = clientOpt.get();

        UUID selectedAddressId = (UUID) session.getAttribute("selectedAddressId");
        List<CartItem> items = cartController.getCartFromSession(session);
        String paymentMethodCode = (String) session.getAttribute("paymentMethod");
        String selectedFreightTypeEnumName = (String) session.getAttribute("freteTipo"); // Enum name, ex: "GADO_RAPIDO"
        BigDecimal valorFrete = (BigDecimal) session.getAttribute("valorFrete"); // VALOR do frete, já calculado

        if (selectedAddressId == null || items == null || items.isEmpty() || paymentMethodCode == null || selectedFreightTypeEnumName == null || valorFrete == null) {
            return "redirect:/checkout";
        }

        Address address = addressRepository.findById(selectedAddressId)
                .orElseThrow(() -> new RuntimeException("Endereço selecionado não encontrado: " + selectedAddressId));

        BigDecimal totalProdutos = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Converte o enumName para o Enum para obter o nome amigável
        Freight freteEnum = Freight.valueOf(selectedFreightTypeEnumName.toUpperCase());
        BigDecimal totalGeral = totalProdutos.add(valorFrete);

        model.addAttribute("clientFirstName", client.getFirstName());
        model.addAttribute("selectedAddress", address);
        model.addAttribute("cartItems", items);
        model.addAttribute("subtotalProdutos", totalProdutos);
        model.addAttribute("freteValor", valorFrete); // Usa o valor recuperado da sessão
        model.addAttribute("freteNome", freteEnum.getNome());
        model.addAttribute("totalPedido", totalGeral);
        model.addAttribute("paymentMethodDisplay", "cartao".equals(paymentMethodCode) ? "Cartão de Crédito" : "Boleto Bancário");

        if ("cartao".equals(paymentMethodCode)) {
            Map<String, String> cardDetails = (Map<String, String>) session.getAttribute("cardDetails");
            if (cardDetails != null && cardDetails.containsKey("installments")) {
                try {
                    int numParcelas = Integer.parseInt(cardDetails.get("installments"));
                    if (numParcelas > 0) {
                        BigDecimal valorParcela = totalGeral.divide(BigDecimal.valueOf(numParcelas), 2, RoundingMode.HALF_UP);
                        model.addAttribute("numeroParcelas", numParcelas);
                        model.addAttribute("valorParcela", valorParcela);
                    }
                } catch (NumberFormatException e) {
                    model.addAttribute("installmentsError", "Número de parcelas inválido.");
                }
            }
        }
        return "checkout/checkout-confirm";
    }

    @PostMapping("/checkout/finalizar")
    public String finalizarCompra(HttpSession session, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Order order = checkoutService.createOrder(principal, session);

            cartController.clearCart(session);
            session.removeAttribute("selectedAddressId");
            session.removeAttribute("paymentMethod");
            session.removeAttribute("freteTipo");
            session.removeAttribute("valorFrete"); // Limpa o valor do frete também
            session.removeAttribute("freteOpcoesDisponiveis");
            session.removeAttribute("cardDetails");

            redirectAttributes.addFlashAttribute("orderNumber", order.getOrderNumber());
            redirectAttributes.addFlashAttribute("orderTotal", order.getTotalPrice());
            redirectAttributes.addFlashAttribute("successMessage", "Pedido realizado com sucesso!");

            return "redirect:/checkout/checkout-success";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("checkoutError", "Erro ao finalizar compra: " + e.getMessage());
            return "redirect:/checkout/confirm";
        }
    }

    @GetMapping("/checkout/checkout-success")
    public String checkoutSucesso(Model model) {
        if (!model.containsAttribute("orderNumber") || !model.containsAttribute("orderTotal")) {
            return "redirect:/";
        }
        return "checkout/checkout-success";
    }
}
