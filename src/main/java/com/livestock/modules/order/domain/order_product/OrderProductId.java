package com.livestock.modules.order.domain.order_product;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class OrderProductId implements Serializable {
    private UUID productId;
    private UUID orderId;

    // Construtor vazio
    public OrderProductId() {
    }

    // Construtor com todos os argumentos
    public OrderProductId(UUID productId, UUID orderId) {
        this.productId = productId;
        this.orderId = orderId;
    }

    // Getters
    public UUID getProductId() {
        return productId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    // Setters
    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    // Implementação manual de equals e hashCode (substituindo @EqualsAndHashCode)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderProductId that = (OrderProductId) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, orderId);
    }
}
