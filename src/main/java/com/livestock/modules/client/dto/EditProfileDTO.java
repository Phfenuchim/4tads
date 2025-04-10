package com.livestock.modules.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class EditProfileDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String fullName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "A data de nascimento deve estar no passado")
    private Date dateBirth;

    @NotBlank(message = "O gênero é obrigatório")
    private String gender;
}
