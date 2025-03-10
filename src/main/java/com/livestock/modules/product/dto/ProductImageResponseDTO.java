package com.livestock.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageResponseDTO {
    private Long id;
    private Boolean defaultImage;
    private String pathUrl;
}
