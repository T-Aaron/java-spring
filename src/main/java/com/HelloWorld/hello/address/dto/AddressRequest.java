package com.HelloWorld.hello.address.dto;

import jakarta.validation.constraints.NotBlank;

public class AddressRequest {
    //@JsonProperty("City") // Chấp nhận key "City" từ JSON nếu muốn viết hoa
    @NotBlank(message = "REQUIRED")
    private String city;
    @NotBlank(message = "REQUIRED")
    private String street;


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
