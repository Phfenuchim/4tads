package com.livestock.modules.user.domain.role;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Table(name="tb_role")
@Entity
public class Role {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String name;

    // Construtor vazio
    public Role() {
    }

    // Construtor com todos os argumentos
    public Role(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Equals e HashCode baseados no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ToString para facilitar depuração
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
