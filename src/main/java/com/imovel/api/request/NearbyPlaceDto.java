package com.imovel.api.request;

public class NearbyPlaceDto {
    private String placeType;
    private String name;
    private String distance;

    // Constructors, Getters, and Setters
    public NearbyPlaceDto() {}
    public NearbyPlaceDto(String placeType, String name, String distance) {
        this.placeType = placeType;
        this.name = name;
        this.distance = distance;
    }

    public String getPlaceType() { return placeType; }
    public void setPlaceType(String placeType) { this.placeType = placeType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
}
