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
                .image(toProductImageResponseDTO(
                        product.getImages().stream()
                                .filter(Product_image::getDefaultImage) // Filtra apenas a imagem padrão
                                .findFirst() // Pega a primeira encontrada
                                .orElse(null) // Se não encontrar nenhuma, retorna null
                ))
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
