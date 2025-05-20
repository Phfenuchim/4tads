package com.livestock.common.dto;

import java.util.Objects;

public class PaginationResponseDTO {
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private int totalItems;

    // Construtor vazio
    public PaginationResponseDTO() {
    }

    // Construtor com todos os argumentos
    public PaginationResponseDTO(int pageNumber, int pageSize, int totalPages, int totalItems) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    // Getters
    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }

    // Setters
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    // ToString para depuração
    @Override
    public String toString() {
        return "PaginationResponseDTO{" +
                "pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", totalItems=" + totalItems +
                '}';
    }

    // Implementação manual do padrão Builder
    public static PaginationResponseDTOBuilder builder() {
        return new PaginationResponseDTOBuilder();
    }

    public static class PaginationResponseDTOBuilder {
        private int pageNumber;
        private int pageSize;
        private int totalPages;
        private int totalItems;

        PaginationResponseDTOBuilder() {
        }

        public PaginationResponseDTOBuilder pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PaginationResponseDTOBuilder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PaginationResponseDTOBuilder totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public PaginationResponseDTOBuilder totalItems(int totalItems) {
            this.totalItems = totalItems;
            return this;
        }

        public PaginationResponseDTO build() {
            return new PaginationResponseDTO(pageNumber, pageSize, totalPages, totalItems);
        }
    }
}
