package com.livestock.modules.checkout.services;

import com.livestock.modules.cart.controllers.CartController;
import com.livestock.modules.cart.dto.CartItem;
// Removido import de FreteConfig se não for mais usado diretamente para calcular aqui
// import com.livestock.modules.order.infra.apis.FreteConfig;
import com.livestock.modules.checkout.payment.PaymentProcessor;
import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.client.repositories.AddressRepository;
import com.livestock.modules.client.repositories.ClientRepository;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.domain.order_product.OrderProduct;
import com.livestock.modules.order.domain.order_status.OrderStatus;
import com.livestock.modules.order.infra.apis.ConsultaCepAPI;
// Removido CepResultDTO se não for usado para recalcular frete aqui
// import com.livestock.modules.order.infra.apis.CepResultDTO;
import com.livestock.modules.order.repositories.OrderProductRepository;
import com.livestock.modules.order.repositories.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map; // Apenas se 'paymentDetails' ainda precisar dele
import java.util.UUID;

@Service
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final CartController cartController;
    private final PaymentProcessor paymentProcessor;
    private final ConsultaCepAPI consultaCepAPI; // Mantido se ainda usado para outras coisas

    // REMOVIDO: Mapa estático FRETE_POR_ESTADO
    // private static final Map<String, BigDecimal> FRETE_POR_ESTADO = Map.ofEntries(...);

    public CheckoutService(OrderRepository orderRepository,
                           OrderProductRepository orderProductRepository,
                           ClientRepository clientRepository,
                           AddressRepository addressRepository,
                           CartController cartController,
                           ConsultaCepAPI consultaCepAPI) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.clientRepository = clientRepository;
        this.addressRepository = addressRepository;
        this.cartController = cartController;
        this.paymentProcessor = new PaymentProcessor();
        this.consultaCepAPI = consultaCepAPI;
    }

    @Transactional
    public Order createOrder(Principal principal, HttpSession session) {
        String username = principal.getName();
        Client client = clientRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + username));

        UUID selectedAddressId = (UUID) session.getAttribute("selectedAddressId");
        String paymentMethodCodeFromSession = (String) session.getAttribute("paymentMethod");
        String freteTipo = (String) session.getAttribute("freteTipo");
        BigDecimal valorFreteCalculado = (BigDecimal) session.getAttribute("valorFrete"); // Valor JÁ CALCULADO
        List<CartItem> items = cartController.getCartFromSession(session);

        // Validações ...
        if (selectedAddressId == null) { /* ... */ }
        if (items == null || items.isEmpty()) { /* ... */ }
        if (paymentMethodCodeFromSession == null || paymentMethodCodeFromSession.trim().isEmpty()) { /* ... */ }
        if (freteTipo == null || freteTipo.trim().isEmpty()) { /* ... */ }
        if (valorFreteCalculado == null) {
            throw new IllegalStateException("Valor do frete não foi calculado ou não encontrado na sessão.");
        }

        Address address = addressRepository.findById(selectedAddressId)
                .orElseThrow(() -> new IllegalArgumentException("Endereço de entrega não encontrado para o ID: " + selectedAddressId));

        // O valor do frete (valorFreteCalculado) vem da sessão, calculado anteriormente pelo FreteController (usando FreteConfig)

        BigDecimal totalProdutos = items.stream()
                .filter(item -> item != null && item.getPrice() != null && item.getQuantity() > 0)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGeral = totalProdutos.add(valorFreteCalculado);
        Long orderNumber = generateOrderNumber();

        Order order = new Order();
        order.setUserId(client.getId());
        order.setTotalPrice(totalGeral.doubleValue());
        order.setCep(address.getCep());
        order.setAddress(address.getStreet());
        order.setAddressNumber(address.getNumber());
        order.setComplement(address.getComplement());
        order.setShipping(valorFreteCalculado.doubleValue());
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.AGUARDANDO_PAGAMENTO);

        Short paymentMethodIdDatabase = null;
        if ("cartao".equalsIgnoreCase(paymentMethodCodeFromSession)) {
            paymentMethodIdDatabase = 1;
        } else if ("boleto".equalsIgnoreCase(paymentMethodCodeFromSession)) {
            paymentMethodIdDatabase = 2;
        }

        if (paymentMethodIdDatabase != null) {
            order.setPaymentMethodId(paymentMethodIdDatabase);
        } else {
            System.err.println("AVISO: Código do método de pagamento '" + paymentMethodCodeFromSession + "' não reconhecido.");
        }

        Order savedOrder = orderRepository.save(order);

        for (CartItem item : items) {
            if (item == null || item.getProductId() == null) continue;
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrderId(savedOrder.getId());
            orderProduct.setProductId(item.getProductId());
            orderProduct.setQuantity(item.getQuantity());

            // IMPORTANTE: Definir o preço unitário do produto NO MOMENTO DA COMPRA
            // Assumindo que CartItem tem um getPrice() que retorna BigDecimal
            orderProduct.setUnitPrice(item.getPrice()); // <<----- ADICIONE ESTA LINHA

            // Se você também quiser manter a referência ao Product (para nome, imagem, etc.),
            // você pode buscar o produto aqui ou confiar que o `item.getProductId()`
            // será usado para popular a relação `Product product` em OrderProduct.
            // Mas para o preço, o `unitPrice` acima é crucial.
            // Product productEntity = productRepository.findById(item.getProductId()).orElse(null);
            // orderProduct.setProduct(productEntity); // Opcional, mas pode ser útil

            orderProductRepository.save(orderProduct);
        }
        return savedOrder;
    }

    // REMOVIDO: Método getFreteBasePorEstado(String uf)
    // public BigDecimal getFreteBasePorEstado(String uf) { ... }

    private Long generateOrderNumber() {
        return 100000 + (long) (Math.random() * 900000);
    }

    public boolean validatePaymentDetails(String paymentMethod, Map<String, String> paymentDetails) {
        return paymentProcessor.validatePayment(paymentMethod, paymentDetails);
    }
}
