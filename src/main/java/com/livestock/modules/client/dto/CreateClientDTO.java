package com.livestock.modules.client.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

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
    @Pattern(regexp = "^\\(\\d{2}\\)\\s\\d{5}\\-\\d{4}$", message = "Formato de telefone inválido")
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

    @Valid
    @NotNull(message = "O endereço de faturamento é obrigatório")
    private AddressDTO billingAddress;

    @Valid
    private List<AddressDTO> shippingAddresses;

    // Construtor vazio
    public CreateClientDTO() {
    }

    // Construtor com todos os argumentos
    public CreateClientDTO(String fullName, String cpf, String email, String phone, Date dateBirth,
                           String gender, String password, AddressDTO billingAddress,
                           List<AddressDTO> shippingAddresses) {
        this.fullName = fullName;
        this.cpf = cpf;
        this.email = email;
        this.phone = phone;
        this.dateBirth = dateBirth;
        this.gender = gender;
        this.password = password;
        this.billingAddress = billingAddress;
        this.shippingAddresses = shippingAddresses;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Date getDateBirth() {
        return dateBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getPassword() {
        return password;
    }

    public AddressDTO getBillingAddress() {
        return billingAddress;
    }

    public List<AddressDTO> getShippingAddresses() {
        return shippingAddresses;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDateBirth(Date dateBirth) {
        this.dateBirth = dateBirth;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBillingAddress(AddressDTO billingAddress) {
        this.billingAddress = billingAddress;
    }

    public void setShippingAddresses(List<AddressDTO> shippingAddresses) {
        this.shippingAddresses = shippingAddresses;
    }
}
