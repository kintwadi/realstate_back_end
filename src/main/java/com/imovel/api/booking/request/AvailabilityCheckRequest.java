package com.imovel.api.booking.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class AvailabilityCheckRequest {

    private Long propertyId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private Boolean instantBookOnly = false;

    // Constructors
    public AvailabilityCheckRequest() {}

    public AvailabilityCheckRequest(Long propertyId, LocalDate checkInDate, LocalDate checkOutDate, 
                                   Integer numberOfAdults, Integer numberOfChildren) {
        this.propertyId = propertyId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
    }

    // Getters and Setters
    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(Integer numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(Integer numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    public Boolean getInstantBookOnly() {
        return instantBookOnly;
    }

    public void setInstantBookOnly(Boolean instantBookOnly) {
        this.instantBookOnly = instantBookOnly;
    }

    // Validation methods
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return checkOutDate.isAfter(checkInDate);
    }

    @AssertTrue(message = "Stay duration cannot exceed 365 days")
    public boolean isValidStayDuration() {
        if (checkInDate == null || checkOutDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return checkInDate.plusDays(365).isAfter(checkOutDate);
    }

    // Helper methods
    public int getTotalGuests() {
        return (numberOfAdults != null ? numberOfAdults : 0) + 
               (numberOfChildren != null ? numberOfChildren : 0);
    }

    public long getStayDurationInDays() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}
