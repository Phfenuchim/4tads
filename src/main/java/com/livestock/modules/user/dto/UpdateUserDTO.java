package com.livestock.modules.user.dto;

import java.util.Objects;
import java.util.UUID;

public class UpdateUserDTO {
    private String name;
    private String cpf;
    private UUID roleId;

    // Construtor vazio
    public UpdateUserDTO() {
    }

    // Construtor com todos os argumentos
    public UpdateUserDTO(String name, String cpf, UUID roleId) {
        this.name = name;
        this.cpf = cpf;
        this.roleId = roleId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCpf() {
        return cpf;
    }

    public UUID getRoleId() {
        return roleId;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    // ToString para depuração
    @Override
    public String toString() {
        return "UpdateUserDTO{" +
                "name='" + name + '\'' +
                ", cpf='" + cpf + '\'' +
                ", roleId=" + roleId +
                '}';
    }

    // Implementação manual do padrão Builder
    public static UpdateUserDTOBuilder builder() {
        return new UpdateUserDTOBuilder();
    }

    public static class UpdateUserDTOBuilder {
        private String name;
        private String cpf;
        private UUID roleId;

        UpdateUserDTOBuilder() {
        }

        public UpdateUserDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UpdateUserDTOBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public UpdateUserDTOBuilder roleId(UUID roleId) {
            this.roleId = roleId;
            return this;
        }

        public UpdateUserDTO build() {
            return new UpdateUserDTO(name, cpf, roleId);
        }
    }
}
