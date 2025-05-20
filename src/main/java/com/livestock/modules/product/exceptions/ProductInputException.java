package com.livestock.modules.product.exceptions;

public class ProductInputException extends RuntimeException {
    public ProductInputException(String errorMessage) {
        super(errorMessage);
    }
}
