package com.livestock.modules.order.dto;

import com.livestock.modules.order.domain.order_status.OrderStatus; // Se for usar o enum diretamente
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDetailDTO {
    private UUID id;
    private Long orderNumber;
    private String status; // Ou OrderStatus status;
    private LocalDateTime createdAt;
    private String fullAddress; // Endereço completo formatado
    private String cep;
    private List<OrderItemDetailDTO> items;
    private BigDecimal subtotalProdutos; // Soma dos subtotais dos itens
    private BigDecimal shippingCost;
    private BigDecimal grandTotal;
    private String paymentMethodName;

    // Construtor
    public OrderDetailDTO(UUID id, Long orderNumber, String status, LocalDateTime createdAt,
                          String fullAddress, String cep, List<OrderItemDetailDTO> items,
                          BigDecimal subtotalProdutos, BigDecimal shippingCost, BigDecimal grandTotal,
                          String paymentMethodName) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.fullAddress = fullAddress;
        this.cep = cep;
        this.items = items;
        this.subtotalProdutos = subtotalProdutos;
        this.shippingCost = shippingCost;
        this.grandTotal = grandTotal;
        this.paymentMethodName = paymentMethodName;
    }

    // Getters
    public UUID getId() { return id; }
    public Long getOrderNumber() { return orderNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getFullAddress() { return fullAddress; }
    public String getCep() { return cep; }
    public List<OrderItemDetailDTO> getItems() { return items; }
    public BigDecimal getSubtotalProdutos() { return subtotalProdutos; }
    public BigDecimal getShippingCost() { return shippingCost; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public String getPaymentMethodName() { return paymentMethodName; }
}
