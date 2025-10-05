package com.imovel.api.booking.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PropertyAvailabilityRequest {

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @Min(value = 1, message = "Minimum stay must be at least 1 day")
    @Max(value = 365, message = "Minimum stay cannot exceed 365 days")
    private Integer minStay;

    @Min(value = 1, message = "Maximum stay must be at least 1 day")
    @Max(value = 365, message = "Maximum stay cannot exceed 365 days")
    private Integer maxStay;

    @Size(max = 200, message = "Blocked reason cannot exceed 200 characters")
    private String blockedReason;

    private Boolean isInstantBook;

    private Boolean checkInAllowed;

    private Boolean checkOutAllowed;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    // Constructors
    public PropertyAvailabilityRequest() {}

    public PropertyAvailabilityRequest(Long propertyId, LocalDate date, Boolean isAvailable) {
        this.propertyId = propertyId;
        this.date = date;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
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

    // Validation methods
    @AssertTrue(message = "Maximum stay must be greater than or equal to minimum stay")
    public boolean isValidStayRange() {
        if (minStay == null || maxStay == null) {
            return true; // Allow partial validation
        }
        return maxStay >= minStay;
    }

    @AssertTrue(message = "Blocked reason is required when property is not available")
    public boolean isBlockedReasonValid() {
        if (isAvailable != null && !isAvailable) {
            return blockedReason != null && !blockedReason.trim().isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Instant booking is only available when property is available")
    public boolean isInstantBookValid() {
        if (isInstantBook != null && isInstantBook) {
            return isAvailable != null && isAvailable;
        }
        return true;
    }
}
