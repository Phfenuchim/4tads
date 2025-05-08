package com.livestock.modules.order.repositories;

import com.livestock.modules.order.domain.order_product.OrderProduct;
import com.livestock.modules.order.domain.order_product.OrderProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductId> {

    /**
     * Encontra todos os itens de um pedido específico
     * @param orderId ID do pedido
     * @return Lista de produtos do pedido
     */
    List<OrderProduct> findByOrderId(UUID orderId);

    /**
     * Verifica se um produto específico está em um pedido
     * @param orderId ID do pedido
     * @param productId ID do produto
     * @return Verdadeiro se o produto estiver no pedido
     */
    boolean existsByOrderIdAndProductId(UUID orderId, UUID productId);

    /**
     * Conta o número de itens em um pedido
     * @param orderId ID do pedido
     * @return Número de itens
     */
    long countByOrderId(UUID orderId);

    /**
     * Exclui todos os itens de um pedido específico
     * @param orderId ID do pedido
     */
    void deleteByOrderId(UUID orderId);
}
