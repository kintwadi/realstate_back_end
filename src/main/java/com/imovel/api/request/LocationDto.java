package com.imovel.api.request;

import jakarta.validation.constraints.NotBlank;

public class LocationDto {
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;

    public LocationDto() {}

    public LocationDto(String address, String city, String state, String zipCode, Double latitude, Double longitude) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
