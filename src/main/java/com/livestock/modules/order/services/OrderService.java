package com.livestock.modules.order.services;

import com.livestock.modules.checkout.payment.PaymentMethod;
import com.livestock.modules.checkout.payment.PaymentMethodRepository;
import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.order.domain.order_product.OrderProduct;
import com.livestock.modules.order.domain.order_status.OrderStatus; // IMPORTAR OrderStatus
import com.livestock.modules.order.dto.OrderDetailDTO;
import com.livestock.modules.order.dto.OrderItemDetailDTO;
import com.livestock.modules.order.repositories.OrderRepository;
// O OrderProductRepository não é estritamente necessário aqui se o carregamento LAZY de
// order.getOrderProducts() for tratado corretamente dentro da transação,
// mas mantê-lo não prejudica e pode ser útil para outras operações futuras.
import com.livestock.modules.order.repositories.OrderProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderProductRepository orderProductRepository; // Adicionado ao construtor

    public OrderService(OrderRepository orderRepository,
                        PaymentMethodRepository paymentMethodRepository,
                        OrderProductRepository orderProductRepository) { // Adicionado ao construtor
        this.orderRepository = orderRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.orderProductRepository = orderProductRepository; // Adicionado ao construtor
    }

    /**
     * Busca um pedido pelo seu ID e garante que seus itens (OrderProduct) sejam carregados.
     * Este método é útil para uso interno no serviço.
     *
     * @param orderId O ID do pedido.
     * @return Um Optional contendo o Order com seus itens, ou Optional.empty() se não encontrado.
     */
    @Transactional(readOnly = true)
    public Optional<Order> findOrderWithItemsById(UUID orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Força o carregamento dos orderProducts se o fetch for LAZY
            // Acessar a coleção dentro da sessão transacional é suficiente.
            if (order.getOrderProducts() != null) {
                order.getOrderProducts().size(); // "Toca" na coleção para carregar
            }
            return Optional.of(order);
        }
        return Optional.empty();
    }

    /**
     * Prepara um DTO com todos os detalhes de um pedido para visualização.
     *
     * @param orderId O ID do pedido.
     * @return Um Optional contendo o OrderDetailDTO, ou Optional.empty() se o pedido não for encontrado.
     */
    @Transactional(readOnly = true)
    public Optional<OrderDetailDTO> getOrderDetailView(UUID orderId) {
        Optional<Order> orderOpt = findOrderWithItemsById(orderId); // Reutiliza para buscar a entidade e seus itens

        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderOpt.get();
        List<OrderProduct> orderProducts = order.getOrderProducts();

        List<OrderItemDetailDTO> itemDTOs = orderProducts.stream().map(op -> {
            BigDecimal unitPrice = op.getUnitPrice(); // Assumindo que OrderProduct tem unitPrice
            int quantity = op.getQuantity();
            BigDecimal itemSubtotal = unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
            String productName = (op.getProduct() != null && op.getProduct().getProductName() != null) ? op.getProduct().getProductName() : "Produto Indisponível";
            UUID productId = (op.getProduct() != null) ? op.getProduct().getId() : null;

            return new OrderItemDetailDTO(productId, productName, quantity, unitPrice, itemSubtotal);
        }).collect(Collectors.toList());

        BigDecimal subtotalProdutos = itemDTOs.stream()
                .map(OrderItemDetailDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String fullAddress = String.format("%s, %s%s",
                order.getAddress(),
                order.getAddressNumber(),
                (order.getComplement() != null && !order.getComplement().isBlank() ? " - " + order.getComplement() : "")
        );
        // Adicionar cidade e estado se estiverem na entidade Order
        // Ex: fullAddress += String.format(" - %s/%s", order.getCity(), order.getState());

        String paymentMethodName = "Não informado";
        if (order.getPaymentMethodId() != null) {
            Optional<PaymentMethod> pmOpt = paymentMethodRepository.findById(order.getPaymentMethodId());
            if (pmOpt.isPresent()) {
                paymentMethodName = pmOpt.get().getPaymentName();
            } else {
                if (order.getPaymentMethodId() == 1) paymentMethodName = "Cartão de Crédito";
                else if (order.getPaymentMethodId() == 2) paymentMethodName = "Boleto Bancário";
            }
        }

        OrderDetailDTO orderDetailDTO = new OrderDetailDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus() != null ? order.getStatus().getDescricao() : "Status Desconhecido",
                order.getCreatedAt(),
                fullAddress,
                order.getCep(),
                itemDTOs,
                subtotalProdutos,
                BigDecimal.valueOf(order.getShipping()),
                BigDecimal.valueOf(order.getTotalPrice()),
                paymentMethodName
        );

        return Optional.of(orderDetailDTO);
    }

    /**
     * Busca todos os pedidos feitos por um cliente específico, ordenados por data decrescente.
     *
     * @param clientId O ID do cliente.
     * @return Uma lista de Orders.
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByClientId(UUID clientId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(clientId); // Assumindo que este método existe no OrderRepository
    }

    // --- NOVOS MÉTODOS PARA O ESTOQUISTA ---

    /**
     * Busca todos os pedidos do sistema, ordenados por data de criação decrescente.
     * Usado pela tela de gerenciamento de pedidos do estoquista/admin.
     *
     * @return Uma lista de todos os Orders.
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrdersSortedByDateDesc() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        // Forçar o carregamento LAZY dos itens para cada pedido, se necessário para a view
        // No entanto, para uma lista, pode ser melhor criar um DTO mais leve
        // que não inclua todos os itens, apenas os dados principais do pedido.
        // Se a tela de lista de pedidos do admin/estoquista precisar mostrar
        // detalhes dos itens, então o carregamento LAZY aqui é importante.
        // Se não, pode-se otimizar para não carregar os itens na lista.
        // Por ora, vamos assumir que a view de lista não precisa dos itens detalhados,
        // e a view de detalhes do pedido (que chama getOrderDetailView) cuidará de carregar os itens.
        return orders;
    }

    /**
     * Atualiza o status de um pedido específico.
     *
     * @param orderId O ID do pedido a ser atualizado.
     * @param newStatus O novo status para o pedido.
     * @return true se o status foi atualizado com sucesso, false se o pedido não foi encontrado.
     */
    @Transactional
    public boolean updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // (Opcional, mas recomendado) Adicionar lógica de validação de transição de status aqui:
            // Por exemplo:
            // if (order.getStatus() == OrderStatus.ENTREGUE && newStatus != OrderStatus.ENTREGUE) {
            //     throw new IllegalStateException("Não é possível alterar o status de um pedido já entregue, exceto para ele mesmo.");
            // }
            // if (order.getStatus() == OrderStatus.CANCELADO && newStatus != OrderStatus.CANCELADO) {
            //     throw new IllegalStateException("Não é possível alterar o status de um pedido cancelado, exceto para ele mesmo.");
            // }
            // Mais regras podem ser adicionadas conforme a necessidade do seu fluxo.

            order.setStatus(newStatus);
            orderRepository.save(order);
            return true;
        }
        return false; // Pedido não encontrado
    }
}
