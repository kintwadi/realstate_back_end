package com.imovel.api.request;

import com.imovel.api.model.enums.PropertyCategory;
import com.imovel.api.model.enums.PropertyStatus;
import com.imovel.api.model.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class PropertyRequestDto {
    private String title;

    private String description;

    private Double price;

    private PropertyType type;

    private PropertyCategory category;

    private Integer bedrooms;
    private Integer bathrooms;

    private Double area;

    private LocationDto location;

    private Set<String> amenities; // Using Set as discussed

    private PropertyStatus status; // Usually set on creation or updated separately

    // Constructors, Getters, and Setters
    public PropertyRequestDto() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public PropertyCategory getCategory() {
        return category;
    }

    public void setCategory(PropertyCategory category) {
        this.category = category;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }

    public Set<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(Set<String> amenities) {
        this.amenities = amenities;
    }

    public PropertyStatus getStatus() {
        return status;
    }

    public void setStatus(PropertyStatus status) {
        this.status = status;
    }
}