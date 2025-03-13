package com.livestock.modules.product.repositories;

import com.livestock.modules.product.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByProductNameContainingIgnoreCase(String productName);

    List<Product> findByActive(boolean active);

    Optional<Product> findByProductName(String productName);

    boolean existsByProductName(String productName);

    @Transactional
    @Modifying
    @Query(""" 
            UPDATE Product p 
            SET p.active = :active 
            WHERE p.id = :id 
            """)
    int updateActiveProduct(@Param("id") UUID id, @Param("active") boolean active);

    @Transactional
    @Modifying
    @Query(""" 
            UPDATE Product p 
            SET p.price = :price 
            WHERE p.id = :id 
            """)
    int updateProductPrice(@Param("id") UUID id, @Param("price") BigDecimal price);

    @Transactional
    @Modifying
    @Query(""" 
            UPDATE Product p 
            SET p.quantity = :quantity 
            WHERE p.id = :id 
            """)
    int updateProductQuantity(@Param("id") UUID id, @Param("quantity") Integer quantity);
}
