package com.livestock.modules.client.dto;

public class AddressResponseDTO {
    private String street;
    private String district;
    private String city;
    private String state;
    private String country;

    // Construtor vazio
    public AddressResponseDTO() {
    }

    // Construtor com todos os argumentos
    public AddressResponseDTO(String street, String district, String city, String state, String country) {
        this.street = street;
        this.district = district;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    // Getters
    public String getStreet() {
        return street;
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

    // Setters
    public void setStreet(String street) {
        this.street = street;
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
}
