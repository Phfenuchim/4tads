package com.livestock.modules.client.dto;

import com.livestock.modules.client.domain.address.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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

    // Construtor vazio
    public NewAddressDTO() {
    }

    // Construtor com todos os argumentos
    public NewAddressDTO(String cep, String street, String number, String complement,
                         String district, String city, String state, String country,
                         boolean isDefault, AddressType type) {
        this.cep = cep;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.district = district;
        this.city = city;
        this.state = state;
        this.country = country;
        this.isDefault = isDefault;
        this.type = type;
    }

    // Getters
    public String getCep() {
        return cep;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public AddressType getType() {
        return type;
    }

    // Setters
    public void setCep(String cep) {
        this.cep = cep;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setType(AddressType type) {
        this.type = type;
    }
}
