package com.livestock.modules.order.domain.order;

import com.livestock.modules.checkout.payment.PaymentMethod;
import com.livestock.modules.client.domain.client.Client;
import com.livestock.modules.order.domain.order_product.OrderProduct; // IMPORTAR
import com.livestock.modules.order.domain.order_status.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList; // IMPORTAR
import java.util.List;      // IMPORTAR
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

    @Column(nullable = false) // Você tinha nullable = false, se pode ser nulo, mude para true
    private String complement;

    @Column(nullable = false)
    private double shipping;

    @ManyToOne(fetch = FetchType.LAZY) // FetchType.LAZY é geralmente uma boa prática aqui
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private Client client; // Relação com o Cliente

    @Column(name = "payment_method_id")
    private Short paymentMethodId;

    @Column(name = "order_number", nullable = false, unique = true)
    private Long orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // NOVA RELAÇÃO @OneToMany com OrderProduct
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderProduct> orderProducts = new ArrayList<>();


    // Construtor vazio
    public Order() {
    }

    // Construtor com argumentos (ajuste conforme necessário)
    // ... seu construtor existente ...
    public Order(UUID id, UUID userId, double totalPrice, LocalDateTime createdAt,
                 String cep, String address, String addressNumber, String complement,
                 double shipping, Short paymentMethodId, Long orderNumber, OrderStatus status) { // Adicionado orderNumber e status
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.cep = cep;
        this.address = address;
        this.addressNumber = addressNumber;
        this.complement = complement;
        this.shipping = shipping;
        this.paymentMethodId = paymentMethodId;
        this.orderNumber = orderNumber;
        this.status = status;
    }

    // Getters e Setters (incluindo para orderProducts)

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public double getShipping() {
        return shipping;
    }

    public void setShipping(double shipping) {
        this.shipping = shipping;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Short getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Short paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

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

    // Getter e Setter para orderProducts
    public List<OrderProduct> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    // Métodos Helper (opcional, mas boa prática para gerenciar a relação bidirecional)
    public void addOrderProduct(OrderProduct orderProduct) {
        this.orderProducts.add(orderProduct);
        orderProduct.setOrder(this); // Mantém a consistência da relação bidirecional
    }

    public void removeOrderProduct(OrderProduct orderProduct) {
        this.orderProducts.remove(orderProduct);
        orderProduct.setOrder(null); // Mantém a consistência
    }


    // Equals e HashCode baseados no ID (como você já tinha)
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

    // ToString (como você já tinha, pode querer adicionar orderProducts se for útil para debug)
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                // ... outros campos ...
                '}';
    }
}
