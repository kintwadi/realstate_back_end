package com.imovel.api.request;

import com.imovel.api.model.enums.PropertyCategory;
import com.imovel.api.model.enums.PropertyStatus;
import com.imovel.api.model.enums.PropertyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class PropertyRequestDto {
    private String mainTitle;
    private PropertyType type;
    private PropertyCategory category;
    private BigDecimal price;
    private String keywords;
    @Valid // Validate nested DTO
    private LocationDto location;
    private String contactPhone;
    private String contactEmail;
    @Valid
    private List<NearbyPlaceDto> nearbyPlaces;
    private String area;

    private Integer bedrooms;
    private Integer bathrooms;
    private Integer parkingSpots;
    private Integer maxAdultsAccommodation;
    private Integer maxChildrenAccommodation;
    private String website;
    private String description;
    private Set<String> amenities;
    private boolean enableAccordionWidget;
    @Valid
    private List<AccordionItemDto> accordionItems;
    private boolean showSimilarProperties;
    private boolean showPriceChangeDynamics;
    private boolean showGoogleMaps;
    private PropertyStatus status;

    // Constructors, Getters, and Setters for all fields
    public PropertyRequestDto() {}

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
    public LocationDto getLocation() { return location; }
    public void setLocation(LocationDto location) { this.location = location; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public List<NearbyPlaceDto> getNearbyPlaces() { return nearbyPlaces; }
    public void setNearbyPlaces(List<NearbyPlaceDto> nearbyPlaces) { this.nearbyPlaces = nearbyPlaces; }
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
    public List<AccordionItemDto> getAccordionItems() { return accordionItems; }
    public void setAccordionItems(List<AccordionItemDto> accordionItems) { this.accordionItems = accordionItems; }
    public boolean isShowSimilarProperties() { return showSimilarProperties; }
    public void setShowSimilarProperties(boolean showSimilarProperties) { this.showSimilarProperties = showSimilarProperties; }
    public boolean isShowPriceChangeDynamics() { return showPriceChangeDynamics; }
    public void setShowPriceChangeDynamics(boolean showPriceChangeDynamics) { this.showPriceChangeDynamics = showPriceChangeDynamics; }
    public boolean isShowGoogleMaps() { return showGoogleMaps; }
    public void setShowGoogleMaps(boolean showGoogleMaps) { this.showGoogleMaps = showGoogleMaps; }
    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }
}