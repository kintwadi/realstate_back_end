package com.imovel.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.imovel.api.repository.PropertyRepository;
import com.imovel.api.services.PropertyService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SampleProperties {

    @Autowired
    PropertyRepository propertyRepository;
    @Transactional
    public static void loadSampleProperties() {
        try {
            // Load the JSON file from resources
            InputStream inputStream = SampleProperties.class.getClassLoader()
                    .getResourceAsStream("sample_properties.json");
            
            if (inputStream == null) {
                System.err.println("File not found in resources folder");
                return;
            }

            // Configure Jackson to handle Java 8 dates and pretty printing
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            // Read the JSON file
            PropertyListing propertyListing = objectMapper.readValue(inputStream, PropertyListing.class);

            // Process each property
            for (Property property : propertyListing.getProperties()) {
                System.out.println("\nProcessed Property:");
                System.out.println("ID: " + property.getId());
                System.out.println("Title: " + property.getMainTitle());
                System.out.println("Type: " + property.getType());
                System.out.println("Price: $" + property.getPrice());
                System.out.println("Location: " + property.getLocation().getCity() + ", " + 
                                  property.getLocation().getState());
                System.out.println("Bedrooms: " + property.getBedrooms());
                System.out.println("Bathrooms: " + property.getBathrooms());
                System.out.println("Status: " + property.getStatus());
                
                // Print media URLs
                System.out.println("Media URLs:");
                for (Media media : property.getMedia()) {
                    System.out.println(" - " + media.getUrl() + " (Primary: " + media.isPrimary() + ")");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Supporting classes to match your entity structure

class PropertyListing {
    private List<Property> properties;

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}

class Property {
    private Long id;
    private String mainTitle;
    private PropertyType type;
    private PropertyCategory category;
    private BigDecimal price;
    private String keywords;
    private Location location;
    private String contactPhone;
    private String contactEmail;
    private List<NearbyPlace> nearbyPlaces = new ArrayList<>();
    private String area;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer parkingSpots;
    private Integer maxAdultsAccommodation;
    private Integer maxChildrenAccommodation;
    private String website;
    private String description;
    private Set<String> amenities = new HashSet<>();
    private boolean enableAccordionWidget;
    private List<AccordionItem> accordionItems = new ArrayList<>();
    private boolean showSimilarProperties;
    private boolean showPriceChangeDynamics;
    private boolean showGoogleMaps;
    private PropertyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Media> media = new ArrayList<>();

    // Getters and setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMainTitle() { return mainTitle; }
    public void setMainTitle(String mainTitle) { this.mainTitle = mainTitle; }
    public PropertyType getType() { return type; }
    public void setType(PropertyType type) { this.type = type; }
    public PropertyCategory getCategory() { return category; }
    public void setCategory(PropertyCategory category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public List<NearbyPlace> getNearbyPlaces() { return nearbyPlaces; }
    public void setNearbyPlaces(List<NearbyPlace> nearbyPlaces) { this.nearbyPlaces = nearbyPlaces; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }
    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }
    public Integer getParkingSpots() { return parkingSpots; }
    public void setParkingSpots(Integer parkingSpots) { this.parkingSpots = parkingSpots; }
    public Integer getMaxAdultsAccommodation() { return maxAdultsAccommodation; }
    public void setMaxAdultsAccommodation(Integer maxAdultsAccommodation) { this.maxAdultsAccommodation = maxAdultsAccommodation; }
    public Integer getMaxChildrenAccommodation() { return maxChildrenAccommodation; }
    public void setMaxChildrenAccommodation(Integer maxChildrenAccommodation) { this.maxChildrenAccommodation = maxChildrenAccommodation; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<String> getAmenities() { return amenities; }
    public void setAmenities(Set<String> amenities) { this.amenities = amenities; }
    public boolean isEnableAccordionWidget() { return enableAccordionWidget; }
    public void setEnableAccordionWidget(boolean enableAccordionWidget) { this.enableAccordionWidget = enableAccordionWidget; }
    public List<AccordionItem> getAccordionItems() { return accordionItems; }
    public void setAccordionItems(List<AccordionItem> accordionItems) { this.accordionItems = accordionItems; }
    public boolean isShowSimilarProperties() { return showSimilarProperties; }
    public void setShowSimilarProperties(boolean showSimilarProperties) { this.showSimilarProperties = showSimilarProperties; }
    public boolean isShowPriceChangeDynamics() { return showPriceChangeDynamics; }
    public void setShowPriceChangeDynamics(boolean showPriceChangeDynamics) { this.showPriceChangeDynamics = showPriceChangeDynamics; }
    public boolean isShowGoogleMaps() { return showGoogleMaps; }
    public void setShowGoogleMaps(boolean showGoogleMaps) { this.showGoogleMaps = showGoogleMaps; }
    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Media> getMedia() { return media; }
    public void setMedia(List<Media> media) { this.media = media; }
}

class Location {
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private double latitude;
    private double longitude;

    // Getters and setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}

class NearbyPlace {
    private String placeType;
    private String name;
    private String distance;

    // Getters and setters
    public String getPlaceType() { return placeType; }
    public void setPlaceType(String placeType) { this.placeType = placeType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
}

class AccordionItem {
    private String title;
    private String details;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}

class Media {
    private Long id;
    private String url;
    private MediaType type;
    private String format;
    private Integer width;
    private Integer height;
    private Long size;
    private Integer duration;
    private boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public MediaType getType() { return type; }
    public void setType(MediaType type) { this.type = type; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

enum PropertyType {
    SALE, RENT, COMMERCIAL
}

enum PropertyCategory {
    HOUSE, APARTMENT, HOTEL, VILLA, OFFICE
}

enum PropertyStatus {
    AVAILABLE, PENDING, SOLD, RENTED
}

enum MediaType {
    IMAGE, VIDEO, DOCUMENT
}