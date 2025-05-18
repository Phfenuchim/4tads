package com.livestock.modules.cart.adapter;

import com.livestock.modules.cart.dto.CartItem;
import com.livestock.modules.cart.composite.CartCompositeGroup;
import com.livestock.modules.cart.composite.CartItemLeaf;

import java.util.List;

public class CartAdapter {

    public static CartCompositeGroup toCompositeGroup(List<CartItem> items) {
        CartCompositeGroup group = new CartCompositeGroup();
        for (CartItem item : items) {
            group.add(new CartItemLeaf(item));
        }
        return group;
    }

}
