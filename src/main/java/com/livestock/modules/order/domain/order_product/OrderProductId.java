package com.livestock.modules.order.domain.order_product;

import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class OrderProductId implements Serializable {
    private UUID productId;
    private UUID orderId;

    public OrderProductId() {}

    public OrderProductId(UUID productId, UUID orderId) {
        this.productId = productId;
        this.orderId = orderId;
    }
}
