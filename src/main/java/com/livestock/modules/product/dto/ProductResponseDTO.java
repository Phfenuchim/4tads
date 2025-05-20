package com.livestock.modules.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProductResponseDTO {
    private UUID id;
    private String productName;
    private Boolean active;
    private Integer quantity;
    private String description;
    private BigDecimal price;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private ProductImageResponseDTO image;
    private List<ProductImageResponseDTO> images;

    // Construtor vazio
    public ProductResponseDTO() {
    }

    // Construtor com todos os argumentos
    public ProductResponseDTO(UUID id, String productName, Boolean active, Integer quantity,
                              String description, BigDecimal price, BigDecimal rating,
                              LocalDateTime createdAt, ProductImageResponseDTO image,
                              List<ProductImageResponseDTO> images) {
        this.id = id;
        this.productName = productName;
        this.active = active;
        this.quantity = quantity;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.createdAt = createdAt;
        this.image = image;
        this.images = images;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public Boolean getActive() {
        return active;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ProductImageResponseDTO getImage() {
        return image;
    }

    public List<ProductImageResponseDTO> getImages() {
        return images;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setImage(ProductImageResponseDTO image) {
        this.image = image;
    }

    public void setImages(List<ProductImageResponseDTO> images) {
        this.images = images;
    }

    // Equals e HashCode baseados no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductResponseDTO that = (ProductResponseDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ToString para debug
    @Override
    public String toString() {
        return "ProductResponseDTO{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", active=" + active +
                ", quantity=" + quantity +
                ", price=" + price +
                ", rating=" + rating +
                '}';
    }

    // Implementação manual do padrão Builder
    public static ProductResponseDTOBuilder builder() {
        return new ProductResponseDTOBuilder();
    }

    public static class ProductResponseDTOBuilder {
        private UUID id;
        private String productName;
        private Boolean active;
        private Integer quantity;
        private String description;
        private BigDecimal price;
        private BigDecimal rating;
        private LocalDateTime createdAt;
        private ProductImageResponseDTO image;
        private List<ProductImageResponseDTO> images;

        ProductResponseDTOBuilder() {
        }

        public ProductResponseDTOBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ProductResponseDTOBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public ProductResponseDTOBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public ProductResponseDTOBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public ProductResponseDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProductResponseDTOBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ProductResponseDTOBuilder rating(BigDecimal rating) {
            this.rating = rating;
            return this;
        }

        public ProductResponseDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ProductResponseDTOBuilder image(ProductImageResponseDTO image) {
            this.image = image;
            return this;
        }

        public ProductResponseDTOBuilder images(List<ProductImageResponseDTO> images) {
            this.images = images;
            return this;
        }

        public ProductResponseDTO build() {
            return new ProductResponseDTO(id, productName, active, quantity, description,
                    price, rating, createdAt, image, images);
        }
    }
}
