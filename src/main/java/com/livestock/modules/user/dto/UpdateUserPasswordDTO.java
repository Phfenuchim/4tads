package com.livestock.modules.user.dto;

import java.util.Objects;

public class UpdateUserPasswordDTO {
    private String newPassword;

    // Construtor vazio
    public UpdateUserPasswordDTO() {
    }

    // Construtor com todos os argumentos
    public UpdateUserPasswordDTO(String newPassword) {
        this.newPassword = newPassword;
    }

    // Getter
    public String getNewPassword() {
        return newPassword;
    }

    // Setter
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    // ToString para depuração
    @Override
    public String toString() {
        return "UpdateUserPasswordDTO{" +
                "newPassword='[PROTECTED]'" + // Não expor a senha no log
                '}';
    }

    // Implementação manual do padrão Builder
    public static UpdateUserPasswordDTOBuilder builder() {
        return new UpdateUserPasswordDTOBuilder();
    }

    public static class UpdateUserPasswordDTOBuilder {
        private String newPassword;

        UpdateUserPasswordDTOBuilder() {
        }

        public UpdateUserPasswordDTOBuilder newPassword(String newPassword) {
            this.newPassword = newPassword;
            return this;
        }

        public UpdateUserPasswordDTO build() {
            return new UpdateUserPasswordDTO(newPassword);
        }
    }
}
