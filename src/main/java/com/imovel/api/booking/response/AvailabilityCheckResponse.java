package com.imovel.api.booking.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AvailabilityCheckResponse {

    private Long propertyId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private Boolean isAvailable;
    private Boolean isInstantBookable;
    private BigDecimal totalPrice;
    private BigDecimal averageNightlyRate;
    private Integer totalNights;
    private Integer minStayRequired;
    private Integer maxStayAllowed;
    private List<String> unavailableDates;
    private List<String> restrictions;
    private List<PropertyAvailabilityResponse> dailyAvailability;
    private String message;

    // Constructors
    public AvailabilityCheckResponse() {}

    public AvailabilityCheckResponse(Long propertyId, LocalDate checkInDate, LocalDate checkOutDate,
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

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Boolean getIsInstantBookable() {
        return isInstantBookable;
    }

    public void setIsInstantBookable(Boolean isInstantBookable) {
        this.isInstantBookable = isInstantBookable;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getAverageNightlyRate() {
        return averageNightlyRate;
    }

    public void setAverageNightlyRate(BigDecimal averageNightlyRate) {
        this.averageNightlyRate = averageNightlyRate;
    }

    public Integer getTotalNights() {
        return totalNights;
    }

    public void setTotalNights(Integer totalNights) {
        this.totalNights = totalNights;
    }

    public Integer getMinStayRequired() {
        return minStayRequired;
    }

    public void setMinStayRequired(Integer minStayRequired) {
        this.minStayRequired = minStayRequired;
    }

    public Integer getMaxStayAllowed() {
        return maxStayAllowed;
    }

    public void setMaxStayAllowed(Integer maxStayAllowed) {
        this.maxStayAllowed = maxStayAllowed;
    }

    public List<String> getUnavailableDates() {
        return unavailableDates;
    }

    public void setUnavailableDates(List<String> unavailableDates) {
        this.unavailableDates = unavailableDates;
    }

    public List<String> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<String> restrictions) {
        this.restrictions = restrictions;
    }

    public List<PropertyAvailabilityResponse> getDailyAvailability() {
        return dailyAvailability;
    }

    public void setDailyAvailability(List<PropertyAvailabilityResponse> dailyAvailability) {
        this.dailyAvailability = dailyAvailability;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Helper methods
    public boolean isBookable() {
        return isAvailable != null && isAvailable;
    }

    public boolean hasRestrictions() {
        return restrictions != null && !restrictions.isEmpty();
    }

    public boolean hasUnavailableDates() {
        return unavailableDates != null && !unavailableDates.isEmpty();
    }

    public boolean meetsMinStayRequirement() {
        if (minStayRequired == null || totalNights == null) {
            return true;
        }
        return totalNights >= minStayRequired;
    }

    public boolean exceedsMaxStayLimit() {
        if (maxStayAllowed == null || totalNights == null) {
            return false;
        }
        return totalNights > maxStayAllowed;
    }

    public boolean hasValidStayDuration() {
        return meetsMinStayRequirement() && !exceedsMaxStayLimit();
    }

    public int getTotalGuests() {
        int adults = numberOfAdults != null ? numberOfAdults : 0;
        int children = numberOfChildren != null ? numberOfChildren : 0;
        return adults + children;
    }

    public String getAvailabilityStatus() {
        if (!isBookable()) {
            return "Not Available";
        }
        if (isInstantBookable != null && isInstantBookable) {
            return "Instant Book Available";
        }
        return "Available - Request to Book";
    }

    public String getStayDurationInfo() {
        if (totalNights == null) {
            return "Duration not calculated";
        }
        
        StringBuilder info = new StringBuilder();
        info.append(totalNights).append(" night");
        if (totalNights > 1) {
            info.append("s");
        }
        
        if (minStayRequired != null && minStayRequired > 1) {
            info.append(" (min ").append(minStayRequired).append(" nights)");
        }
        
        if (maxStayAllowed != null && maxStayAllowed > 0) {
            info.append(" (max ").append(maxStayAllowed).append(" nights)");
        }
        
        return info.toString();
    }

    public String getPriceBreakdown() {
        if (totalPrice == null || totalNights == null || totalNights == 0) {
            return "Price not available";
        }
        
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Total: ").append(totalPrice);
        
        if (averageNightlyRate != null) {
            breakdown.append(" (avg ").append(averageNightlyRate).append("/night)");
        }
        
        return breakdown.toString();
    }

    public boolean requiresApproval() {
        return isBookable() && (isInstantBookable == null || !isInstantBookable);
    }

    public String getBookingInstructions() {
        if (!isBookable()) {
            return "This property is not available for the selected dates.";
        }
        
        if (isInstantBookable != null && isInstantBookable) {
            return "You can book this property instantly.";
        }
        
        return "Send a booking request to the host for approval.";
    }
}
