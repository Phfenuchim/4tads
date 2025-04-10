package com.livestock.modules.client.dto;

import com.livestock.modules.client.domain.address.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "^\\d{8}$", message = "CEP inválido")
    private String cep;

    @NotBlank(message = "O logradouro é obrigatório")
    private String street;

    @NotBlank(message = "O número é obrigatório")
    private String number;

    private String complement;

    @NotBlank(message = "O bairro é obrigatório")
    private String district;

    @NotBlank(message = "A cidade é obrigatória")
    private String city;

    @NotBlank(message = "O estado é obrigatório")
    private String state;

    @NotBlank(message = "O país é obrigatório")
    private String country;

    private boolean isDefault;

    @NotNull(message = "O tipo de endereço é obrigatório")
    private AddressType type;

}
