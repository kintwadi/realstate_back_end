package com.imovel.api.booking.model;

import com.imovel.api.model.Property;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "property_availability", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "date"}))
public class PropertyAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "price", precision = 19, scale = 4)
    private BigDecimal price;

    @PositiveOrZero
    @Column(name = "min_stay")
    private Integer minStay = 1;

    @PositiveOrZero
    @Column(name = "max_stay")
    private Integer maxStay = 365;

    @Column(name = "blocked_reason", length = 255)
    private String blockedReason;

    @Column(name = "is_instant_book", nullable = false)
    private Boolean isInstantBook = false;

    @Column(name = "check_in_allowed", nullable = false)
    private Boolean checkInAllowed = true;

    @Column(name = "check_out_allowed", nullable = false)
    private Boolean checkOutAllowed = true;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public PropertyAvailability() {}

    public PropertyAvailability(Property property, LocalDate date, Boolean isAvailable, BigDecimal price) {
        this.property = property;
        this.date = date;
        this.isAvailable = isAvailable;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getMinStay() {
        return minStay;
    }

    public void setMinStay(Integer minStay) {
        this.minStay = minStay;
    }

    public Integer getMaxStay() {
        return maxStay;
    }

    public void setMaxStay(Integer maxStay) {
        this.maxStay = maxStay;
    }

    public String getBlockedReason() {
        return blockedReason;
    }

    public void setBlockedReason(String blockedReason) {
        this.blockedReason = blockedReason;
    }

    public Boolean getIsInstantBook() {
        return isInstantBook;
    }

    public void setIsInstantBook(Boolean isInstantBook) {
        this.isInstantBook = isInstantBook;
    }

    public Boolean getCheckInAllowed() {
        return checkInAllowed;
    }

    public void setCheckInAllowed(Boolean checkInAllowed) {
        this.checkInAllowed = checkInAllowed;
    }

    public Boolean getCheckOutAllowed() {
        return checkOutAllowed;
    }

    public void setCheckOutAllowed(Boolean checkOutAllowed) {
        this.checkOutAllowed = checkOutAllowed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business methods
    public void blockDate(String reason) {
        this.isAvailable = false;
        this.blockedReason = reason;
    }

    public void unblockDate() {
        this.isAvailable = true;
        this.blockedReason = null;
    }

    public boolean isBookableForStay(int nights) {
        return isAvailable && nights >= minStay && nights <= maxStay;
    }

    public BigDecimal getEffectivePrice(Property property) {
        return price != null ? price : property.getPrice();
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyAvailability that = (PropertyAvailability) o;
        return Objects.equals(property, that.property) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, date);
    }

    @Override
    public String toString() {
        return "PropertyAvailability{" +
                "id=" + id +
                ", date=" + date +
                ", isAvailable=" + isAvailable +
                ", price=" + price +
                '}';
    }
}
