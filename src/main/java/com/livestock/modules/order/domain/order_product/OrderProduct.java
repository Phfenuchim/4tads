package com.livestock.modules.order.domain.order_product;

import com.livestock.modules.order.domain.order.Order;
import com.livestock.modules.product.domain.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tb_order_product")
@IdClass(OrderProductId.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
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
}
