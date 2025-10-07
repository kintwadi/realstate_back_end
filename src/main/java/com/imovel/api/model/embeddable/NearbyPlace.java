package com.imovel.api.model.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class NearbyPlace {

    @Column(name = "nearby_place_type")
    private String placeType;

    @Column(name = "nearby_place_name")
    private String name;

    @Column(name = "nearby_place_distance")
    private String distance;

    public NearbyPlace() {}

    public NearbyPlace(String placeType, String name, String distance) {
        this.placeType = placeType;
        this.name = name;
        this.distance = distance;
    }

    // Getters and Setters
    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "NearbyPlace{" +
                "placeType='" + placeType + '\'' +
                ", name='" + name + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }
}
