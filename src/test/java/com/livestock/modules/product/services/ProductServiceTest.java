package com.livestock.modules.product.services;

import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.domain.product_image.Product_image;
import com.livestock.modules.product.dto.CreateProductDTO;
import com.livestock.modules.product.dto.UpdateProductDTO;
import com.livestock.modules.product.exceptions.ProductInputException;
import com.livestock.modules.product.exceptions.ProductNotFoundException;
import com.livestock.modules.product.repositories.ProductImageRepository;
import com.livestock.modules.product.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductImageRepository productImageRepository;

    @InjectMocks private ProductService productService;

    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setProductName("Gado Nelore");
        product.setDescription("Gado forte e saudável.");
        product.setPrice(new BigDecimal("5000.00"));
        product.setQuantity(10);
        product.setActive(true);
    }

    @Test
    void testCreateProductSuccess() {
        CreateProductDTO dto = new CreateProductDTO("Gado Angus", 5, "Descrição", new BigDecimal("10000"), BigDecimal.valueOf(4.5), true);

        when(productRepository.existsByProductName(dto.getProductName())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        var result = productService.createProduct(dto);

        assertNotNull(result);
        assertEquals("Gado Nelore", result.getProductName());
    }

    @Test
    void testCreateProductNameExists() {
        CreateProductDTO dto = new CreateProductDTO("Gado Angus", 5, "Descrição", new BigDecimal("10000"), BigDecimal.valueOf(4.5), true);

        when(productRepository.existsByProductName(dto.getProductName())).thenReturn(true);

        assertThrows(ProductInputException.class, () -> productService.createProduct(dto));
    }

    @Test
    void testUpdateProductActiveStatusSuccess() {
        when(productRepository.updateActiveProduct(productId, false)).thenReturn(1);
        assertTrue(productService.updateProductActiveStatus(productId, false));
    }

    @Test
    void testUpdateProductActiveStatusInvalidId() {
        assertThrows(ProductInputException.class, () -> productService.updateProductActiveStatus(null, true));
    }

    @Test
    void testUpdateProductPriceInvalidPrice() {
        assertThrows(ProductInputException.class, () -> productService.updateProductPrice(productId, BigDecimal.ZERO));
    }

    @Test
    void testUpdateProductSuccess() {
        UpdateProductDTO dto = new UpdateProductDTO("Novo Gado", true, 20, "Nova Descrição", new BigDecimal("7000"), BigDecimal.valueOf(4.8));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        var updated = productService.updateProduct(productId, dto);

        assertEquals("Novo Gado", updated.getProductName());
        assertEquals(new BigDecimal("7000"), updated.getPrice());
        assertTrue(updated.getActive());
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void testAddImageToProductSuccess() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        Product_image image = new Product_image();
        image.setId(1L);
        image.setPathUrl("/images/gado.png");
        image.setDefaultImage(true);

        when(productImageRepository.save(any(Product_image.class))).thenReturn(image);

        var result = productService.addImageToProduct(productId, "/images/gado.png", true);

        assertNotNull(result);
        assertEquals("/images/gado.png", result.getPathUrl());
    }

    @Test
    void testDeleteProductNotFound() {
        when(productRepository.existsById(productId)).thenReturn(false);
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(productId));
    }
}
