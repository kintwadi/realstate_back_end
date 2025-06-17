package com.imovel.api.model;

import com.imovel.api.model.embeddable.AccordionItem;
import com.imovel.api.model.embeddable.NearbyPlace;
import com.imovel.api.model.enums.PropertyCategory;
import com.imovel.api.model.enums.PropertyStatus;
import com.imovel.api.model.enums.PropertyType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String mainTitle; // Was 'title'

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType type; // e.g., SALE, RENT, COMMERCIAL

    @Enumerated(EnumType.STRING)
    @Column(name = "property_category", nullable = false)
    private PropertyCategory category; // e.g., HOUSE, APARTMENT, HOTEL, VILLA, OFFICE

    @Column(nullable = false, precision = 19, scale = 4) // Precision for BigDecimal
    private BigDecimal price; // Changed from Double to BigDecimal

    @Column(name = "keywords", length = 500)
    private String keywords; // Comma-separated

    @Embedded
    private Location location;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Email
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "property_nearby_places", joinColumns = @JoinColumn(name = "property_id"))
    @OrderBy("placeType ASC") // Optional: order the collection
    private List<NearbyPlace> nearbyPlaces = new ArrayList<>();

    @Column(name = "area_description", length = 100)
    private String area;

    @Column(name = "bedrooms")
    private Integer bedrooms;

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "parking_spots")
    private Integer parkingSpots; // For number of parking spots

    @Column(name = "max_adults_accommodation")
    private Integer maxAdultsAccommodation;

    @Column(name = "max_children_accommodation")
    private Integer maxChildrenAccommodation;

    @Column(name = "website_url", length = 2048)
    private String website;

    @Lob
    @Column(name = "property_details_text", columnDefinition = "TEXT") // Renamed from 'description'
    private String description; // This is for "Property Details Text"

    @ElementCollection(fetch = FetchType.LAZY) // For Amenities
    @CollectionTable(name = "property_amenities_new", joinColumns = @JoinColumn(name = "property_id")) // Renamed table to avoid conflict if old one exists
    @Column(name = "amenity_name")
    private Set<String> amenities = new HashSet<>();

    @Column(name = "enable_accordion_widget")
    private boolean enableAccordionWidget;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "property_accordion_items", joinColumns = @JoinColumn(name = "property_id"))
    @OrderBy("title ASC") // Optional
    private List<AccordionItem> accordionItems = new ArrayList<>();

    // 5. Premium Options (Display/Feature Flags - original section 7):
    @Column(name = "show_similar_properties")
    private boolean showSimilarProperties;

    @Column(name = "show_price_change_dynamics")
    private boolean showPriceChangeDynamics;

    @Column(name = "show_Maps")
    private boolean showGoogleMaps;

    // Standard Fields (already existed, ensure they are still relevant)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // Constructors
    public Property() {
        // Initialization of collections
        this.amenities = new HashSet<>();
        this.nearbyPlaces = new ArrayList<>();
        this.accordionItems = new ArrayList<>();
        this.enableAccordionWidget = false;
        this.showSimilarProperties = false;
        this.showPriceChangeDynamics = false;
        this.showGoogleMaps = true;
    }

    // Getters and Setters for all fields
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

    public String getDescription() { return description; } // For "Property Details Text"
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

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; } // Should be handled by @CreationTimestamp

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; } // Should be handled by @UpdateTimestamp

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return Objects.equals(id, property.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Property{" +
                "id=" + id +
                ", mainTitle='" + mainTitle + '\'' +
                ", type=" + type +
                ", category=" + category +
                ", price=" + price +
                '}';
    }
}
