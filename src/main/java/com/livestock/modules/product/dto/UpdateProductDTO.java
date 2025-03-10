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
public class UpdateProductDTO {
    private String productName;
    private Boolean active;
    private Integer quantity;
    private String description;
    private BigDecimal price;
}