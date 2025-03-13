package com.livestock.modules.product.domain.product_image;

import com.livestock.modules.product.domain.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name="tb_product_images")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
}
