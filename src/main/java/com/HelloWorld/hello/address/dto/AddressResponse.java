package com.HelloWorld.hello.address.dto;
import com.HelloWorld.hello.entity.Address;

public class AddressResponse {
    private Long id;
    private String city;
    private String street;

    public AddressResponse(Long id, String city, String street) {
        this.id = id;
        this.city = city;
        this.street = street;
    }

    public AddressResponse() {
    }
    public Long getId() {
        return id;
    }

    public static AddressResponse from(Address address) {
        AddressResponse res = new AddressResponse();
        res.id = address.getId();
        res.city = address.getCity();
        res.street = address.getStreet();

        return res;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
