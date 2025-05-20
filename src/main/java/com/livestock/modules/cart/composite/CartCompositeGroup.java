package com.livestock.modules.cart.composite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartCompositeGroup extends CartComponent {
    private final List<CartComponent> children = new ArrayList<>();

    @Override
    public void add(CartComponent component) {
        children.add(component);
    }

    @Override
    public void remove(CartComponent component) {
        children.remove(component);
    }

    @Override
    public List<CartComponent> getChildren() {
        return children;
    }

    @Override
    public BigDecimal getTotalPrice() {
        return children.stream()
                .map(CartComponent::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getQuantity() {
        return children.stream()
                .mapToInt(CartComponent::getQuantity)
                .sum();
    }
}

