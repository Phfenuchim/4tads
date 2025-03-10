package com.livestock.modules.product.repositories;

import com.livestock.modules.product.domain.product_image.Product_image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<Product_image, Long> {

    List<Product_image> findByProductId(UUID productId);

    Optional<Product_image> findByProductIdAndDefaultImage(UUID productId, boolean defaultImage);

    @Transactional
    @Modifying
    @Query("""
            UPDATE Product_image pi
            SET pi.defaultImage = false
            WHERE pi.product.id = :productId
            """)
    int resetDefaultImagesForProduct(@Param("productId") UUID productId);

    @Transactional
    @Modifying
    @Query("""
            UPDATE Product_image pi
            SET pi.defaultImage = true
            WHERE pi.id = :imageId
            """)
    int setDefaultImage(@Param("imageId") Long imageId);

    @Transactional
    void deleteByProductId(UUID productId);
}
