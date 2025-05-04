package com.livestock.modules.product.dto;

public class CreateProductImageDTO {
    private Boolean defaultImage;
    private String pathUrl;

    // Construtor vazio
    public CreateProductImageDTO() {
    }

    // Construtor com todos os argumentos
    public CreateProductImageDTO(Boolean defaultImage, String pathUrl) {
        this.defaultImage = defaultImage;
        this.pathUrl = pathUrl;
    }

    // Getters
    public Boolean getDefaultImage() {
        return defaultImage;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    // Setters
    public void setDefaultImage(Boolean defaultImage) {
        this.defaultImage = defaultImage;
    }

    public void setPathUrl(String pathUrl) {
        this.pathUrl = pathUrl;
    }

    // Implementação manual do padrão Builder
    public static CreateProductImageDTOBuilder builder() {
        return new CreateProductImageDTOBuilder();
    }

    public static class CreateProductImageDTOBuilder {
        private Boolean defaultImage;
        private String pathUrl;

        CreateProductImageDTOBuilder() {
        }

        public CreateProductImageDTOBuilder defaultImage(Boolean defaultImage) {
            this.defaultImage = defaultImage;
            return this;
        }

        public CreateProductImageDTOBuilder pathUrl(String pathUrl) {
            this.pathUrl = pathUrl;
            return this;
        }

        public CreateProductImageDTO build() {
            return new CreateProductImageDTO(defaultImage, pathUrl);
        }
    }
}
