package com.livestock.modules.order.domain.order;

import com.livestock.modules.order.domain.payment_method.PaymentMethod;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "tb_order")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    @JoinColumn(name = "payment_method_id", updatable = false, insertable = false, nullable = true)
    private PaymentMethod paymentMethod;

}
