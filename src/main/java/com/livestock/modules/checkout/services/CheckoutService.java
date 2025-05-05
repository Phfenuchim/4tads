package com.livestock.modules.checkout.services;

import com.livestock.modules.cart.dto.CartItem;
import com.livestock.modules.checkout.payment.PaymentProcessor;
import com.livestock.modules.checkout.payment.PaymentResult;
import com.livestock.modules.client.domain.address.Address;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutService {

    private final PaymentProcessor paymentProcessor;
    private final OrderRepository orderRepository;

    @Autowired
    public CheckoutService(OrderRepository orderRepository) {
        this.paymentProcessor = new PaymentProcessor();
        this.orderRepository = orderRepository;
    }


    @Transactional
    public Order createOrder(String paymentMethod, Map<String, String> paymentDetails,
                             BigDecimal totalAmount, Client client, Address address,
                             List<CartItem> items, BigDecimal shipping) {

        // Processar pagamento com a estratégia correta
        PaymentResult paymentResult = paymentProcessor.processPayment(
                paymentMethod, totalAmount, paymentDetails);

        if (!paymentResult.isSuccess()) {
            throw new RuntimeException(paymentResult.getMessage()); // Use uma exceção apropriada
        }

        // Criar e salvar o pedido com as informações de pagamento
        Order order = new Order();
        order.setUserId(client.getId());
        order.setTotalPrice(totalAmount.doubleValue());
        order.setCep(address.getCep());
        order.setAddress(address.getStreet());
        order.setAddressNumber(address.getNumber());
        order.setComplement(address.getComplement());
        order.setShipping(shipping.doubleValue());

        // Salvar a transação em algum campo ou tabela relacionada
        // Como não existe setPaymentTransactionId, você pode armazenar
        // essa informação de outra forma, como uma tabela separada

        return orderRepository.save(order);
    }


    public boolean validatePaymentDetails(String paymentMethod, Map<String, String> paymentDetails) {
        // Obter a estratégia correta do processador
        return paymentProcessor.validatePayment(paymentMethod, paymentDetails);
    }

}
