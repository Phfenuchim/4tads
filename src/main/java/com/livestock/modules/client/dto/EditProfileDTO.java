package com.livestock.modules.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class EditProfileDTO {

    @NotBlank(message = "O nome completo é obrigatório.")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]{3,}\\s+[A-Za-zÀ-ÖØ-öø-ÿ]{3,}.*$",
            message = "O nome deve ter pelo menos duas palavras com no mínimo 3 letras cada.")
    private String fullName;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve estar no passado.")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Importante para a conversão do Spring
    private Date dateBirth; // Ou LocalDate se preferir

    @NotBlank(message = "O gênero é obrigatório.")
    private String gender;

    // Construtor vazio
    public EditProfileDTO() {
    }

    // Construtor com todos os argumentos
    public EditProfileDTO(String fullName, Date dateBirth, String gender) {
        this.fullName = fullName;
        this.dateBirth = dateBirth;
        this.gender = gender;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public Date getDateBirth() {
        return dateBirth;
    }

    public String getGender() {
        return gender;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDateBirth(Date dateBirth) {
        this.dateBirth = dateBirth;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
