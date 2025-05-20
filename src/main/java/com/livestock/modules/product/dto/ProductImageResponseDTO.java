package com.livestock.modules.product.dto;

import java.util.Objects;

public class ProductImageResponseDTO {
    private Long id;
    private Boolean defaultImage;
    private String pathUrl;

    // Construtor vazio
    public ProductImageResponseDTO() {
    }

    // Construtor com todos os argumentos
    public ProductImageResponseDTO(Long id, Boolean defaultImage, String pathUrl) {
        this.id = id;
        this.defaultImage = defaultImage;
        this.pathUrl = pathUrl;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Boolean getDefaultImage() {
        return defaultImage;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setDefaultImage(Boolean defaultImage) {
        this.defaultImage = defaultImage;
    }

    public void setPathUrl(String pathUrl) {
        this.pathUrl = pathUrl;
    }

    // Equals e HashCode baseados no id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductImageResponseDTO that = (ProductImageResponseDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Builder pattern manual
    public static ProductImageResponseDTOBuilder builder() {
        return new ProductImageResponseDTOBuilder();
    }

    public static class ProductImageResponseDTOBuilder {
        private Long id;
        private Boolean defaultImage;
        private String pathUrl;

        ProductImageResponseDTOBuilder() {
        }

        public ProductImageResponseDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProductImageResponseDTOBuilder defaultImage(Boolean defaultImage) {
            this.defaultImage = defaultImage;
            return this;
        }

        public ProductImageResponseDTOBuilder pathUrl(String pathUrl) {
            this.pathUrl = pathUrl;
            return this;
        }

        public ProductImageResponseDTO build() {
            return new ProductImageResponseDTO(id, defaultImage, pathUrl);
        }
    }

    // ToString
    @Override
    public String toString() {
        return "ProductImageResponseDTO{" +
                "id=" + id +
                ", defaultImage=" + defaultImage +
                ", pathUrl='" + pathUrl + '\'' +
                '}';
    }
}
