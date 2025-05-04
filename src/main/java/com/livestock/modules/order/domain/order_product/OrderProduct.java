package com.livestock.modules.order.domain.order_product;

import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.product.domain.product.Product;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tb_order_product")
@IdClass(OrderProductId.class)
public class OrderProduct {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    // Construtor vazio
    public OrderProduct() {
    }

    // Construtor com todos os argumentos
    public OrderProduct(UUID productId, UUID orderId, int quantity, Order order, Product product) {
        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.order = order;
        this.product = product;
    }

    // Getters
    public UUID getProductId() {
        return productId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    // Setters
    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // Equals e HashCode baseados na chave composta (productId e orderId)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderProduct that = (OrderProduct) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, orderId);
    }

    // ToString para facilitar depuração
    @Override
    public String toString() {
        return "OrderProduct{" +
                "productId=" + productId +
                ", orderId=" + orderId +
                ", quantity=" + quantity +
                '}';
    }
}
