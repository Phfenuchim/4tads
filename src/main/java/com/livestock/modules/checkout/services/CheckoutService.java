package com.livestock.modules.checkout.services;

import com.livestock.modules.cart.controllers.CartController;
import com.livestock.modules.cart.dto.CartItem;
import com.livestock.modules.checkout.domain.Freight;
import com.livestock.modules.checkout.payment.PaymentProcessor;
import com.livestock.modules.checkout.payment.PaymentResult;
import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.AddressRepository;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.domain.order_product.OrderProduct;
import com.livestock.modules.order.domain.order_status.OrderStatus;
import com.livestock.modules.order.repositories.OrderProductRepository;
import com.livestock.modules.order.repositories.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final CartController cartController;
    private final PaymentProcessor paymentProcessor;

    @Autowired
    public CheckoutService(OrderRepository orderRepository,
                           OrderProductRepository orderProductRepository,
                           ClientRepository clientRepository,
                           AddressRepository addressRepository,
                           CartController cartController) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.clientRepository = clientRepository;
        this.addressRepository = addressRepository;
        this.cartController = cartController;
        this.paymentProcessor = new PaymentProcessor();
    }

    @Transactional
    public Order createOrder(Principal principal, HttpSession session) {
        // Obter cliente
        String username = principal.getName();
        Client client = clientRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + username));

        // Recuperar dados da sessão
        UUID selectedAddressId = (UUID) session.getAttribute("selectedAddressId");
        String paymentMethodCodeFromSession = (String) session.getAttribute("paymentMethod"); // Ex: "cartao", "boleto"
        String freteTipo = (String) session.getAttribute("freteTipo");
        List<CartItem> items = cartController.getCartFromSession(session);

        // Validações
        if (selectedAddressId == null) {
            throw new IllegalStateException("Nenhum endereço de entrega selecionado.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Carrinho vazio. Não é possível criar o pedido.");
        }
        if (paymentMethodCodeFromSession == null || paymentMethodCodeFromSession.trim().isEmpty()) {
            // Decida o que fazer: lançar erro ou permitir pedido sem método de pagamento (se a coluna no DB permitir NULL)
            throw new IllegalStateException("Método de pagamento não selecionado na sessão.");
        }
        if (freteTipo == null || freteTipo.trim().isEmpty()) {
            throw new IllegalStateException("Tipo de frete não selecionado na sessão.");
        }


        Address address = addressRepository.findById(selectedAddressId)
                .orElseThrow(() -> new IllegalArgumentException("Endereço de entrega não encontrado para o ID: " + selectedAddressId));

        // Calcular totais
        BigDecimal totalProdutos = items.stream()
                .filter(item -> item != null && item.getPrice() != null && item.getQuantity() > 0) // Validação básica
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Freight freight = Freight.valueOf(freteTipo.toUpperCase()); // Garanta que freteTipo corresponda ao nome do enum
        BigDecimal freteValor = freight.getValor();

        BigDecimal totalGeral = totalProdutos.add(freteValor);

        // Gerar número sequencial
        Long orderNumber = generateOrderNumber();

        // Criar pedido
        Order order = new Order();
        order.setUserId(client.getId()); // Supondo que Order tenha setUserId(UUID id)
        // Se Order tem @ManyToOne Client client, então seria order.setClient(client);
        order.setTotalPrice(totalGeral.doubleValue()); // Considere usar BigDecimal em Order.totalPrice também
        order.setCep(address.getCep());
        order.setAddress(address.getStreet());
        order.setAddressNumber(address.getNumber());
        order.setComplement(address.getComplement());
        order.setShipping(freteValor.doubleValue()); // Considere usar BigDecimal em Order.shipping
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.AGUARDANDO_PAGAMENTO); // Supondo que OrderStatus seja um enum

        // ---- INÍCIO DA LÓGICA PARA DEFINIR O MÉTODO DE PAGAMENTO ----
        Short paymentMethodIdDatabase = null;
        if ("cartao".equalsIgnoreCase(paymentMethodCodeFromSession)) {
            paymentMethodIdDatabase = 1; // ID para 'Crédito'
        } else if ("boleto".equalsIgnoreCase(paymentMethodCodeFromSession)) {
            paymentMethodIdDatabase = 2; // ID para 'Boleto'
        }
        // Adicionar mais 'else if' para outros métodos (ex: "pix" -> 3)

        if (paymentMethodIdDatabase != null) {
            order.setPaymentMethodId(paymentMethodIdDatabase);

        } else {
            // Lançar exceção ou logar, pois o método de pagamento da sessão não foi reconhecido
            // Se payment_method_id na TB_ORDER for NOT NULL, isso causará um erro de banco se não for setado.
            // Se for NULL, o pedido será criado sem método de pagamento.
            System.err.println("AVISO: Código do método de pagamento '" + paymentMethodCodeFromSession + "' não reconhecido. O pedido será salvo sem método de pagamento específico.");
            // Se a coluna no banco for NOT NULL, você DEVE lançar uma exceção aqui:
            // throw new IllegalArgumentException("Método de pagamento '" + paymentMethodCodeFromSession + "' não é suportado.");
        }
        // ---- FIM DA LÓGICA PARA DEFINIR O MÉTODO DE PAGAMENTO ----

        // Salvar pedido
        Order savedOrder = orderRepository.save(order); // Renomeado para savedOrder para clareza

        // Salvar itens do pedido
        for (CartItem item : items) {
            if (item == null || item.getProductId() == null) continue; // Pular itens nulos ou sem ID de produto

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrderId(savedOrder.getId()); // Usar o ID do pedido salvo
            orderProduct.setProductId(item.getProductId());
            orderProduct.setQuantity(item.getQuantity());
            orderProductRepository.save(orderProduct);
        }
        return savedOrder;
    }

    // Método auxiliar para gerar número sequencial do pedido
    private Long generateOrderNumber() {
        return 100000 + (long) (Math.random() * 900000);
    }

    // Método existente para validar detalhes de pagamento
    public boolean validatePaymentDetails(String paymentMethod, Map<String, String> paymentDetails) {
        return paymentProcessor.validatePayment(paymentMethod, paymentDetails);
    }
}
