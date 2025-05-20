package com.livestock.modules.product.domain.product;

import com.livestock.modules.product.domain.product_image.Product_image;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Table(name="tb_products")
@Entity
public class Product {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Boolean active = true;

    @NotNull(message = "Quantidade não pode ser nula.")
    @Min(value = 0, message = "Quantidade do produto não pode ser negativa.")
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Product_image> images = new HashSet<>();

    // Construtor vazio
    public Product() {
    }

    // Construtor com todos os argumentos
    public Product(UUID id, String productName, Boolean active, Integer quantity,
                   String description, BigDecimal price, BigDecimal rating,
                   LocalDateTime createdAt, Set<Product_image> images) {
        this.id = id;
        this.productName = productName;
        this.active = active;
        this.quantity = quantity;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.createdAt = createdAt;
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

    public Set<Product_image> getImages() {
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
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Quantidade do produto não pode ser negativa.");
        }
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

    public void setImages(Set<Product_image> images) {
        this.images = images;
    }

    // Callback do JPA mantido
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Equals e HashCode baseados no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ToString para facilitar depuração
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", active=" + active +
                ", quantity=" + quantity +
                ", price=" + price +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                '}';
    }
}
