package com.livestock.modules.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class EditProfileDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String fullName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "A data de nascimento deve estar no passado")
    private Date dateBirth;

    @NotBlank(message = "O gênero é obrigatório")
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
