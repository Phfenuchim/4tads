package com.livestock.modules.product.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
