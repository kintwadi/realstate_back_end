package com.imovel.api.booking.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PropertyAvailabilityResponse {

    private Long id;
    private Long propertyId;
    private LocalDate date;
    private Boolean isAvailable;
    private BigDecimal price;
    private Integer minStay;
    private Integer maxStay;
    private String blockedReason;
    private Boolean isInstantBook;
    private Boolean checkInAllowed;
    private Boolean checkOutAllowed;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PropertyAvailabilityResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
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

    // Helper methods
    public boolean isBookable() {
        return isAvailable != null && isAvailable && 
               (blockedReason == null || blockedReason.trim().isEmpty());
    }

    public boolean isBlocked() {
        return !isBookable();
    }

    public boolean hasCustomPricing() {
        return price != null;
    }

    public boolean hasStayRestrictions() {
        return (minStay != null && minStay > 1) || (maxStay != null && maxStay > 0);
    }

    public boolean isInstantBookable() {
        return isBookable() && isInstantBook != null && isInstantBook;
    }

    public boolean allowsCheckIn() {
        return checkInAllowed != null && checkInAllowed;
    }

    public boolean allowsCheckOut() {
        return checkOutAllowed != null && checkOutAllowed;
    }

    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }

    public String getAvailabilityStatus() {
        if (isAvailable == null) {
            return "Unknown";
        }
        if (!isAvailable) {
            return "Unavailable";
        }
        if (blockedReason != null && !blockedReason.trim().isEmpty()) {
            return "Blocked";
        }
        return "Available";
    }

    public String getBookingType() {
        if (!isBookable()) {
            return "Not Bookable";
        }
        if (isInstantBookable()) {
            return "Instant Book";
        }
        return "Request to Book";
    }

    public boolean isWeekend() {
        if (date == null) {
            return false;
        }
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
    }

    public boolean isPastDate() {
        return date != null && date.isBefore(LocalDate.now());
    }

    public boolean isFutureDate() {
        return date != null && date.isAfter(LocalDate.now());
    }

    public boolean isToday() {
        return date != null && date.equals(LocalDate.now());
    }

    public long getDaysFromNow() {
        if (date == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}
