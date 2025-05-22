package com.livestock.modules.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemDetailDTO {
    private UUID productId; // Para o link do produto
    private String productName;
    private int quantity;
    private BigDecimal unitPrice; // Preço unitário no momento da compra
    private BigDecimal subtotal;  // Calculado: unitPrice * quantity

    // Construtor
    public OrderItemDetailDTO(UUID productId, String productName, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    // Getters
    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
