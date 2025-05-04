package com.livestock.modules.product.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class UpdateProductDTO {
    private String productName;
    private Boolean active;
    private Integer quantity;
    private String description;
    private BigDecimal price;
    private BigDecimal rating;

    // Construtor vazio
    public UpdateProductDTO() {
    }

    // Construtor com todos os argumentos
    public UpdateProductDTO(String productName, Boolean active, Integer quantity, String description, BigDecimal price, BigDecimal rating) {
        this.productName = productName;
        this.active = active;
        this.quantity = quantity;
        this.description = description;
        this.price = price;
        this.rating = rating;
    }

    // Getters e Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "UpdateProductDTO{" +
                "productName='" + productName + '\'' +
                ", active=" + active +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                '}';
    }

    // Implementação manual do padrão Builder
    public static UpdateProductDTOBuilder builder() {
        return new UpdateProductDTOBuilder();
    }

    public static class UpdateProductDTOBuilder {
        private String productName;
        private Boolean active;
        private Integer quantity;
        private String description;
        private BigDecimal price;
        private BigDecimal rating;

        UpdateProductDTOBuilder() {
        }

        public UpdateProductDTOBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public UpdateProductDTOBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public UpdateProductDTOBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public UpdateProductDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UpdateProductDTOBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public UpdateProductDTOBuilder rating(BigDecimal rating) {
            this.rating = rating;
            return this;
        }

        public UpdateProductDTO build() {
            return new UpdateProductDTO(productName, active, quantity, description, price, rating);
        }
    }
}
