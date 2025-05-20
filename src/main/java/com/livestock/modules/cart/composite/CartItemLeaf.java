package com.livestock.modules.cart.composite;

import com.livestock.modules.cart.dto.CartItem;

import java.math.BigDecimal;

public class CartItemLeaf extends CartComponent {
    private final CartItem item;

    public CartItemLeaf(CartItem item) {
        this.item = item;
    }

    @Override
    public BigDecimal getTotalPrice() {
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    @Override
    public int getQuantity() {
        return item.getQuantity();
    }

    public CartItem getItem() {
        return item;
    }
}

