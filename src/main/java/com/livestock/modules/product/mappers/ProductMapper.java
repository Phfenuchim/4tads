package com.livestock.modules.product.mappers;

import com.livestock.modules.product.domain.product.Product;
import com.livestock.modules.product.domain.product_image.Product_image;
import com.livestock.modules.product.dto.ProductResponseDTO;
import com.livestock.modules.product.dto.ProductImageResponseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductResponseDTO toProductResponseDTO(Product product) {
        ProductImageResponseDTO defaultImage = null;
        List<ProductImageResponseDTO> allImages = new ArrayList<>();

        if (product.getImages() != null) {
            // Mapeia todas as imagens
            for (Product_image image : product.getImages()) {
                ProductImageResponseDTO imageDto = new ProductImageResponseDTO(
                        image.getId(),
                        image.getDefaultImage(),
                        image.getPathUrl()
                );

                allImages.add(imageDto);

                // Identifica a imagem padrão
                if (image.getDefaultImage()) {
                    defaultImage = imageDto;
                }
            }
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .active(product.getActive())
                .quantity(product.getQuantity())
                .description(product.getDescription())
                .price(product.getPrice())
                .rating(product.getRating())
                .createdAt(product.getCreatedAt())
                .image(defaultImage)
                .images(allImages)
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
