package com.livestock.modules.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateProductDTO {

    @NotBlank(message = "Nome do produto não pode ser vazio!")
    @Size(max = 200, message = "Nome do produto não pode exceder 200 caracteres.")
    private String productName;

    @NotNull(message = "Quantidade não pode ser nula.")
    @Min(value = 0, message = "Quantidade do produto não pode ser negativa.") // Adicionado
    private Integer quantity;

    @Size(max = 2000, message = "Descrição não pode exceder 2000 caracteres.")
    private String description;

    @NotNull(message = "Preço do produto não pode ser nulo.")
    @DecimalMin(value = "0.01", message = "Preço do produto deve ser maior que zero!")
    private BigDecimal price;

    @DecimalMin(value = "1.0", message = "Avaliação deve ser no mínimo 1.")
    @DecimalMax(value = "5.0", message = "Avaliação deve ser no máximo 5.") // Importar DecimalMax
    private BigDecimal rating; // Pode ser nulo se não for obrigatório

    private Boolean active;
    // Construtor vazio
    public CreateProductDTO() {
    }

    // Construtor com todos os argumentos
    public CreateProductDTO(String productName, Integer quantity, String description,
                            BigDecimal price, BigDecimal rating, Boolean active) {
        this.productName = productName;
        this.quantity = quantity;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.active = active;
    }

    // Implementação manual do padrão Builder em substituição à anotação @Builder
    public static CreateProductDTOBuilder builder() {
        return new CreateProductDTOBuilder();
    }

    public static class CreateProductDTOBuilder {
        private String productName;
        private Integer quantity;
        private String description;
        private BigDecimal price;
        private BigDecimal rating;
        private Boolean active;

        CreateProductDTOBuilder() {
        }

        public CreateProductDTOBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public CreateProductDTOBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public CreateProductDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateProductDTOBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public CreateProductDTOBuilder rating(BigDecimal rating) {
            this.rating = rating;
            return this;
        }

        public CreateProductDTOBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public CreateProductDTO build() {
            return new CreateProductDTO(productName, quantity, description, price, rating, active);
        }
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public Boolean getActive() {
        return active;
    }

    // Setters
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // toString()
    @Override
    public String toString() {
        return "CreateProductDTO{" +
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                ", active=" + active +
                '}';
    }
}
