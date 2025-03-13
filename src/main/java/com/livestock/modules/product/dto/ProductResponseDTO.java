package com.livestock.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
}