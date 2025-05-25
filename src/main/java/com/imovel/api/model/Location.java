package com.imovel.api.model; //

import jakarta.persistence.Embeddable;

@Embeddable
public class Location { // As described in Property's location: Object field
    private String address;
    private String city;
    private String state;
    private String zipCode; // Assuming this might be part of the address object
    private Double latitude;
    private Double longitude;

    public Location() {}

    // Getters and Setters for Location
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