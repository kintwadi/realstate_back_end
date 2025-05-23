package com.imovel.api.model;


import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a property listing
 */
@Data
public class Property {
    private String id;
    private String title;
    private String description;
    private Double price;
    private PropertyType type;
    private PropertyCategory category;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double area;
    private Location location;
    private List<String> amenities;
    private List<String> images;
    private PropertyStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}