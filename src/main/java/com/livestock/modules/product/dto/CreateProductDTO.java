package com.livestock.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProductDTO {
    private String productName;
    private Integer quantity;
    private String description;
    private BigDecimal price;
    private BigDecimal rating;
}