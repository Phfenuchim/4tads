package com.livestock.modules.product.mappers;

import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.domain.product_image.Product_image;
import com.livestock.modules.product.dto.ProductResponseDTO;
import com.livestock.modules.product.dto.ProductImageResponseDTO;

import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductResponseDTO toProductResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .active(product.getActive())
                .quantity(product.getQuantity())
                .description(product.getDescription())
                .price(product.getPrice())
                .rating(product.getRating())
                .createdAt(product.getCreatedAt())
                .images(product.getImages().stream()
                        .map(ProductMapper::toProductImageResponseDTO)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static ProductImageResponseDTO toProductImageResponseDTO(Product_image image) {
        return ProductImageResponseDTO.builder()
                .id(image.getId())
                .defaultImage(image.getDefaultImage())
                .pathUrl(image.getPathUrl())
                .build();
    }
}
