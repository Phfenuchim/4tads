package com.livestock.modules.product.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Objects;

public class UpdateProductDTO {

    @Size(max = 200, message = "Nome do produto não pode exceder 200 caracteres.")
    private String productName;

    private Boolean active = true; // Ou false, dependendo do default desejado

    @Min(value = 0, message = "Quantidade do produto não pode ser negativa.") // Adicionado
    private Integer quantity;

    @Size(max = 2000, message = "Descrição não pode exceder 2000 caracteres.")
    private String description;

    @DecimalMin(value = "0.01", inclusive = false, message = "Preço do produto deve ser maior que zero!") // inclusive=false se não puder ser 0.01
    private BigDecimal price;

    @DecimalMin(value = "1.0", message = "Avaliação deve ser no mínimo 1.")
    @DecimalMax(value = "5.0", message = "Avaliação deve ser no máximo 5.")
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
        return active != null ? active : false; // Garante que nunca retorne null
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
