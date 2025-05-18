package com.livestock.modules.cart.composite;

import java.math.BigDecimal;
import java.util.List;

public abstract class CartComponent {
    public BigDecimal getTotalPrice() {
        throw new UnsupportedOperationException();
    }
    public int getQuantity() {
        throw new UnsupportedOperationException();
    }
    public void add(CartComponent component) {
        throw new UnsupportedOperationException();
    }
    public void remove(CartComponent component) {
        throw new UnsupportedOperationException();
    }
    public List<CartComponent> getChildren() {
        return List.of();
    }
}

