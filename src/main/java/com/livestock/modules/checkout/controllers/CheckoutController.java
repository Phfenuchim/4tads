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
import com.livestock.modules.order.infra.apis.ConsultaCepAPI; // Suponho que esta classe chama sua API de CEP/Frete
import com.livestock.modules.order.infra.apis.FreteResponse; // Suponho que esta seja a resposta da sua API de CEP/Frete
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

/**
 * Controller responsável por gerenciar o fluxo de checkout da aplicação.
 * Lida com as etapas de seleção de endereço, pagamento, confirmação e finalização do pedido.
 */
@Controller
public class CheckoutController {

    // Injeção das dependências necessárias para o funcionamento do checkout.
    private final AddressRepository addressRepository;
    private final CartController cartController; // Para interagir com o carrinho (armazenado na sessão).
    private final ClientRepository clientRepository; // Para buscar dados do cliente.
    private final CheckoutService checkoutService; // Serviço que contém a lógica de negócio do checkout.
    private final ConsultaCepAPI consultaCepAPI; // Para consultar informações de CEP/Frete.

    // Construtor para injeção de dependências.
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

    /**
     * Exibe a primeira etapa do checkout: seleção de endereço.
     * Carrega os endereços do cliente e os itens do carrinho para exibição.
     */
    @GetMapping("/checkout")
    public String initialCheckout(Model model, Principal principal, HttpSession session) {
        // Obtém o email do usuário logado.
        String username = principal.getName();
        // Busca o cliente no banco de dados.
        Optional<Client> clientOpt = clientRepository.findByEmail(username);

        // Se o cliente não for encontrado, redireciona para o login.
        if (clientOpt.isEmpty()) {
            return "redirect:/login";
        }
        Client client = clientOpt.get();

        // Busca os endereços cadastrados para o cliente.
        List<Address> addresses = addressRepository.findByClientId(client.getId());

        // Se não houver endereços, redireciona para a página de cadastro de endereços.
        // O usuário precisa ter pelo menos um endereço para prosseguir.
        if (addresses.isEmpty()) {
            // Talvez redirecionar com um parâmetro para voltar ao checkout depois
            // ex: "redirect:/client/addresses?redirectTo=/checkout"
            return "redirect:/client/addresses?redirectTo=/checkout";
        }

        // Obtém os itens do carrinho da sessão.
        List<CartItem> items = cartController.getCartFromSession(session);
        if (items == null || items.isEmpty()) {
            // Se o carrinho estiver vazio, redireciona para o carrinho ou home
            return "redirect:/cart";
        }

        // Calcula o subtotal dos produtos no carrinho.
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Adiciona os dados ao Model para serem usados na view.
        model.addAttribute("userFirstName", client.getFirstName()); // Nome do usuário para saudação.
        model.addAttribute("addresses", addresses); // Lista de endereços para seleção.
        model.addAttribute("items", items); // Itens do carrinho para resumo.
        model.addAttribute("subtotal", total); // Subtotal dos produtos.

        // Retorna a view da primeira etapa do checkout.
        return "checkout/initial-checkout";
    }

    /**
     * Processa a seleção do endereço pelo usuário.
     * Armazena o ID do endereço selecionado na sessão e redireciona para a etapa de pagamento.
     */
    @PostMapping("/checkout/select-address")
    public String selectAddress(@RequestParam("addressId") UUID addressId, HttpSession session) {
        // Armazena o ID do endereço escolhido na sessão para uso posterior.
        session.setAttribute("selectedAddressId", addressId);
        // Redireciona para a próxima etapa: seleção de pagamento e frete.
        return "redirect:/checkout/payment";
    }

    /**
     * Exibe a etapa de seleção de método de pagamento e opções de frete.
     */
    @GetMapping("/checkout/payment")
    public String checkoutPayment(Model model, HttpSession session) {
        // Recupera o ID do endereço selecionado na etapa anterior.
        UUID addressId = (UUID) session.getAttribute("selectedAddressId");

        // Se nenhum endereço foi selecionado (ex: acesso direto à URL), volta para a primeira etapa.
        if (addressId == null) {
            return "redirect:/checkout";
        }

        // Busca o objeto Address completo.
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado para o ID: " + addressId));

        // Consulta as opções de frete para o CEP do endereço selecionado.
        // "consultaCepViaApiInterna" sugere que esta API já retorna as opções de frete.
        FreteResponse freteResponse = consultaCepAPI.consultaCepViaApiInterna(address.getCep());

        // Adiciona as opções de frete ao Model e também à sessão para uso posterior.
        model.addAttribute("freteOpcoes", freteResponse.getOpcoesFrete()); // Para exibir na tela
        session.setAttribute("freteOpcoesDisponiveis", freteResponse.getOpcoesFrete()); // Para validação ou referência

        // Retorna a view da etapa de pagamento e frete.
        return "checkout/checkout-payment";
    }


    /**
     * Valida os dados de pagamento e frete submetidos pelo usuário.
     * Armazena as informações na sessão e redireciona para a confirmação.
     */
    @PostMapping("/checkout/validate")
    public String validateRequest(@RequestParam String paymentMethod, // "cartao" ou "boleto"
                                  @RequestParam String freteTipo,   // Código/nome do tipo de frete escolhido
                                  @RequestParam Map<String, String> allParams, // Todos os parâmetros do formulário
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) { // Para mensagens de erro

        // Armazena o método de pagamento e o tipo de frete escolhidos na sessão.
        session.setAttribute("paymentMethod", paymentMethod.toLowerCase()); // Padroniza para minúsculas
        session.setAttribute("freteTipo", freteTipo); // Ex: "GADO_RAPIDO"

        // Se o método for cartão, coleta e valida os detalhes do cartão.
        if ("cartao".equalsIgnoreCase(paymentMethod)) {
            Map<String, String> cardDetails = new HashMap<>();
            cardDetails.put("cardName", allParams.get("cardName"));
            cardDetails.put("cardNumber", allParams.get("cardNumber"));
            cardDetails.put("cardCvv", allParams.get("cardCvv"));
            cardDetails.put("cardExpiry", allParams.get("cardExpiry"));
            cardDetails.put("installments", allParams.get("installments"));

            try {
                // Usa o CheckoutService para validar os detalhes do cartão com a estratégia apropriada.
                boolean valid = checkoutService.validatePaymentDetails(paymentMethod.toLowerCase(), cardDetails);
                if (!valid) {
                    redirectAttributes.addFlashAttribute("paymentError", "Dados do cartão inválidos. Verifique as informações.");
                    return "redirect:/checkout/payment"; // Volta para a tela de pagamento com erro
                }

                // Se válido, armazena os detalhes do cartão na sessão (cuidado com dados sensíveis).
                // Idealmente, apenas um token ou referência ao pagamento seria armazenado.
                // Para este exemplo, estamos armazenando diretamente, mas em produção isso seria revisado.
                session.setAttribute("cardDetails", cardDetails); // Armazena o mapa todo
                // Ou individualmente, como você estava fazendo:
                // for (Map.Entry<String, String> entry : cardDetails.entrySet()) {
                //     session.setAttribute(entry.getKey(), entry.getValue());
                // }

            } catch (IllegalArgumentException e) { // Captura exceções da validação (ex: método não suportado)
                redirectAttributes.addFlashAttribute("paymentError", e.getMessage());
                return "redirect:/checkout/payment";
            }
        }
        // Se for boleto, não há validação de campos extras nesta etapa (a validação seria ter escolhido 'boleto').

        // Redireciona para a tela de confirmação do pedido.
        return "redirect:/checkout/confirm";
    }

    /**
     * Exibe a tela de confirmação do pedido, com todos os detalhes para revisão final.
     */
    @GetMapping("/checkout/confirm")
    public String confirmRequest(Model model, HttpSession session, Principal principal) {
        // Verifica e obtém os dados necessários da sessão e do usuário.
        String username = principal.getName();
        Optional<Client> clientOpt = clientRepository.findByEmail(username);

        if (clientOpt.isEmpty()) { return "redirect:/login"; } // Cliente não logado
        Client client = clientOpt.get();

        UUID selectedAddressId = (UUID) session.getAttribute("selectedAddressId");
        List<CartItem> items = cartController.getCartFromSession(session);
        String paymentMethodCode = (String) session.getAttribute("paymentMethod"); // "cartao" ou "boleto"
        String selectedFreightType = (String) session.getAttribute("freteTipo");   // Ex: "GADO_RAPIDO"

        // Validações de segurança: se algum dado essencial da sessão sumiu, volta ao início do checkout.
        if (selectedAddressId == null || items == null || items.isEmpty() || paymentMethodCode == null || selectedFreightType == null) {
            return "redirect:/checkout";
        }

        // Busca o endereço selecionado.
        Address address = addressRepository.findById(selectedAddressId)
                .orElseThrow(() -> new RuntimeException("Endereço selecionado não encontrado: " + selectedAddressId));

        // Calcula totais novamente para garantir consistência (ou recupera da sessão se já calculado).
        BigDecimal totalProdutos = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Freight freteEnum = Freight.valueOf(selectedFreightType.toUpperCase()); // Converte a string para o Enum
        BigDecimal valorFrete = freteEnum.getValor();
        BigDecimal totalGeral = totalProdutos.add(valorFrete);

        // Adiciona os dados ao Model para exibição na tela de confirmação.
        model.addAttribute("clientFirstName", client.getFirstName());
        model.addAttribute("selectedAddress", address);
        model.addAttribute("cartItems", items);
        model.addAttribute("subtotalProdutos", totalProdutos);
        model.addAttribute("freteValor", valorFrete);
        model.addAttribute("freteNome", freteEnum.getNome()); // Nome amigável do frete
        model.addAttribute("totalPedido", totalGeral);
        model.addAttribute("paymentMethodDisplay", "cartao".equals(paymentMethodCode) ? "Cartão de Crédito" : "Boleto Bancário"); // Nome para exibição

        // Se for cartão e houver parcelas, calcula e adiciona ao modelo.
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
                    // Tratar erro se 'installments' não for um número válido
                    model.addAttribute("installmentsError", "Número de parcelas inválido.");
                }
            }
        }

        // Retorna a view de confirmação.
        return "checkout/checkout-confirm";
    }

    /**
     * Processa a finalização da compra.
     * Chama o CheckoutService para criar o pedido no banco de dados.
     * Limpa a sessão e redireciona para a página de sucesso ou erro.
     */
    @PostMapping("/checkout/finalizar")
    public String finalizarCompra(HttpSession session, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            // Delegação para o CheckoutService, que contém a lógica transacional de criação do pedido.
            Order order = checkoutService.createOrder(principal, session);

            // Limpa os atributos de checkout da sessão APÓS o pedido ser criado com sucesso.
            cartController.clearCart(session); // Limpa o carrinho
            session.removeAttribute("selectedAddressId");
            session.removeAttribute("paymentMethod");
            session.removeAttribute("freteTipo");
            session.removeAttribute("freteOpcoesDisponiveis");
            session.removeAttribute("cardDetails"); // E outros detalhes de cartão se armazenados individualmente

            // Adiciona dados do pedido aos FlashAttributes para serem exibidos na página de sucesso.
            redirectAttributes.addFlashAttribute("orderNumber", order.getOrderNumber());
            redirectAttributes.addFlashAttribute("orderTotal", order.getTotalPrice());
            redirectAttributes.addFlashAttribute("successMessage", "Pedido realizado com sucesso!"); // Mensagem de sucesso

            return "redirect:/checkout/checkout-success";
        } catch (Exception e) {
            // Em caso de erro (ex: estoque indisponível, falha ao salvar),
            // redireciona de volta para a tela de confirmação com uma mensagem de erro.
            e.printStackTrace(); // Logar a exceção para depuração no servidor
            redirectAttributes.addFlashAttribute("checkoutError", "Erro ao finalizar compra: " + e.getMessage());
            return "redirect:/checkout/confirm"; // Volta para a tela de confirmação para o usuário tentar novamente ou ver o erro.
        }
    }

    /**
     * Exibe a página de sucesso após a finalização da compra.
     * Os dados do pedido (número, total) são passados via FlashAttributes.
     */
    @GetMapping("/checkout/checkout-success")
    public String checkoutSucesso(Model model) {
        // Verifica se os atributos flash (orderNumber, orderTotal) estão presentes no modelo.
        // Se não estiverem (ex: acesso direto à URL), redireciona para a home.
        if (!model.containsAttribute("orderNumber") || !model.containsAttribute("orderTotal")) {
            return "redirect:/"; // Ou para a página de pedidos do usuário
        }
        // Os atributos flash já estarão no model se vierem de um redirect com RedirectAttributes.
        return "checkout/checkout-success";
    }
}
