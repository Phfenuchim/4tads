package com.livestock.modules.user.dto;

import com.livestock.modules.user.domain.role.Role;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private boolean active;
    private String cpf;
    private Set<Role> roles = new HashSet<>();

    // Construtor vazio
    public UserResponseDTO() {
    }

    // Construtor com todos os argumentos
    public UserResponseDTO(UUID id, String name, String email, boolean active, String cpf, Set<Role> roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = active;
        this.cpf = cpf;
        this.roles = roles;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public String getCpf() {
        return cpf;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // Equals e HashCode baseados no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseDTO that = (UserResponseDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ToString para depuração
    @Override
    public String toString() {
        return "UserResponseDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", cpf='" + cpf + '\'' +
                ", roles=" + roles +
                '}';
    }

    // Implementação manual do padrão Builder
    public static UserResponseDTOBuilder builder() {
        return new UserResponseDTOBuilder();
    }

    public static class UserResponseDTOBuilder {
        private UUID id;
        private String name;
        private String email;
        private boolean active;
        private String cpf;
        private Set<Role> roles = new HashSet<>();

        UserResponseDTOBuilder() {
        }

        public UserResponseDTOBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public UserResponseDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserResponseDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserResponseDTOBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public UserResponseDTOBuilder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public UserResponseDTOBuilder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public UserResponseDTO build() {
            return new UserResponseDTO(id, name, email, active, cpf, roles);
        }
    }
}
