package com.livestock.modules.product.domain.product_image;

import com.livestock.modules.product.domain.product.Product;
import jakarta.persistence.*;
import java.util.Objects;

@Table(name="tb_product_images")
@Entity
public class Product_image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "default_image")
    private Boolean defaultImage = false;

    @Column(name = "path_url", nullable = false)
    private String pathUrl;

    // Construtor vazio
    public Product_image() {
    }

    // Construtor com todos os argumentos
    public Product_image(Long id, Product product, Boolean defaultImage, String pathUrl) {
        this.id = id;
        this.product = product;
        this.defaultImage = defaultImage;
        this.pathUrl = pathUrl;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Boolean getDefaultImage() {
        return defaultImage;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setDefaultImage(Boolean defaultImage) {
        this.defaultImage = defaultImage;
    }

    public void setPathUrl(String pathUrl) {
        this.pathUrl = pathUrl;
    }

    // Implementação de equals e hashCode baseados no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product_image that = (Product_image) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Implementação de toString (opcional mas útil para debug)
    @Override
    public String toString() {
        return "Product_image{" +
                "id=" + id +
                ", defaultImage=" + defaultImage +
                ", pathUrl='" + pathUrl + '\'' +
                '}';
    }
}
