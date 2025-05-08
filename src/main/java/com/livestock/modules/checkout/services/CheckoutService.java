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
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        // Recuperar dados da sessão
        UUID selectedAddressId = (UUID) session.getAttribute("selectedAddressId");
        String paymentMethod = (String) session.getAttribute("paymentMethod");
        String freteTipo = (String) session.getAttribute("freteTipo");
        List<CartItem> items = cartController.getCartFromSession(session);

        // Validações
        if (items.isEmpty()) {
            throw new IllegalStateException("Carrinho vazio");
        }

        Address address = addressRepository.findById(selectedAddressId)
                .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado"));

        // Calcular totais
        BigDecimal totalProdutos = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Freight freight = Freight.valueOf(freteTipo);
        BigDecimal freteValor = freight.getValor();

        BigDecimal totalGeral = totalProdutos.add(freteValor);

        // Gerar número sequencial
        Long orderNumber = generateOrderNumber();

        // Criar pedido
        Order order = new Order();
        order.setUserId(client.getId());
        order.setTotalPrice(totalGeral.doubleValue());
        order.setCep(address.getCep());
        order.setAddress(address.getStreet());
        order.setAddressNumber(address.getNumber());
        order.setComplement(address.getComplement());
        order.setShipping(freteValor.doubleValue());
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.AGUARDANDO_PAGAMENTO);

        // Salvar pedido
        order = orderRepository.save(order);

        // Salvar itens do pedido
        for (CartItem item : items) {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrderId(order.getId());
            orderProduct.setProductId(item.getProductId());
            orderProduct.setQuantity(item.getQuantity());
            orderProductRepository.save(orderProduct);
        }

        return order;
    }

    // Método auxiliar para gerar número sequencial do pedido
    private Long generateOrderNumber() {
        // Versão simplificada - em produção considere usar uma sequência do banco de dados
        return 100000 + (long) (Math.random() * 900000);
    }

    // Método existente para validar detalhes de pagamento
    public boolean validatePaymentDetails(String paymentMethod, Map<String, String> paymentDetails) {
        return paymentProcessor.validatePayment(paymentMethod, paymentDetails);
    }
}
