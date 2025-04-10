package com.livestock.modules.client.dto;

import com.livestock.modules.client.domain.address.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewAddressDTO {

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
    private String cep;

    @NotBlank(message = "Rua é obrigatória")
    private String street;

    @NotBlank(message = "Número é obrigatório")
    private String number;

    private String complement;

    @NotBlank(message = "Bairro é obrigatório")
    private String district;

    @NotBlank(message = "Cidade é obrigatória")
    private String city;

    @NotBlank(message = "Estado é obrigatório")
    private String state;

    @NotBlank(message = "País é obrigatório")
    private String country;

    private boolean isDefault;

    @NotNull(message = "O tipo de endereço é obrigatório")
    private AddressType type;
}

