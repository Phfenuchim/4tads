package com.livestock.modules.order.domain.order;

import com.livestock.modules.checkout.payment.PaymentMethod;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.order.domain.order_status.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table(name = "tb_order")
@Entity
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String cep;

    @Column(nullable = false)
    private String address;

    @Column(name = "address_number", nullable = false)
    private String addressNumber;

    @Column(nullable = false)
    private String complement;

    @Column(nullable = false)
    private double shipping;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private Client client;


    @ManyToOne
    @JoinColumn(name = "payment_method_id", updatable = false, insertable = false, nullable = true)
    private PaymentMethod paymentMethod;

    // Adicione à entidade Order
    @Column(name = "order_number", nullable = false, unique = true)
    private Long orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // Getters e Setters para os novos campos
    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }


    // Construtor vazio
    public Order() {
    }

    // Construtor com todos os argumentos
    public Order(UUID id, UUID userId, double totalPrice, LocalDateTime createdAt,
                 String cep, String address, String addressNumber, String complement,
                 double shipping, PaymentMethod paymentMethod) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.cep = cep;
        this.address = address;
        this.addressNumber = addressNumber;
        this.complement = complement;
        this.shipping = shipping;
        this.paymentMethod = paymentMethod;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCep() {
        return cep;
    }

    public String getAddress() {
        return address;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public String getComplement() {
        return complement;
    }

    public double getShipping() {
        return shipping;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public void setShipping(double shipping) {
        this.shipping = shipping;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Equals e HashCode baseados no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ToString para facilitar depuração
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                ", cep='" + cep + '\'' +
                ", shipping=" + shipping +
                '}';
    }
}
