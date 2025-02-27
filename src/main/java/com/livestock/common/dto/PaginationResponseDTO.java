package com.livestock.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaginationResponseDTO {
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private int totalItems;
}
