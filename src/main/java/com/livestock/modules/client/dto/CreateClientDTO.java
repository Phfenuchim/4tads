package com.livestock.modules.client.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateClientDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ]{3,}\\s+[A-Za-zÀ-ÖØ-öø-ÿ]{3,}.*$",
            message = "O nome deve ter pelo menos duas palavras com no mínimo 3 letras cada")
    private String fullName;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$", message = "CPF inválido")
    private String cpf;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    private String phone;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve estar no passado")
    private Date dateBirth;


    @NotBlank(message = "O gênero é obrigatório")
    private String gender;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotNull(message = "O endereço de faturamento é obrigatório")
    private AddressDTO billingAddress;

    private List<AddressDTO> shippingAddresses;
}
