package com.imovel.api.booking.request;

import com.imovel.api.booking.model.enums.CancellationPolicyType;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CancellationPolicyRequest {

    private Long propertyId;
    private CancellationPolicyType policyType;
    private BigDecimal refundPercentage;
    private Integer daysBeforeCheckIn;
    private String description;
    private Boolean isActive;

    // Constructors
    public CancellationPolicyRequest() {}

    public CancellationPolicyRequest(Long propertyId, CancellationPolicyType policyType) {
        this.propertyId = propertyId;
        this.policyType = policyType;
        this.isActive = true;
        
        // Set default values based on policy type
        if (policyType != null) {
            this.refundPercentage = policyType.getDefaultRefundPercentage();
            this.daysBeforeCheckIn = policyType.getDefaultDaysBeforeCheckin();
            this.description = policyType.getDescription();
        }
    }

    // Getters and Setters
    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public CancellationPolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(CancellationPolicyType policyType) {
        this.policyType = policyType;
    }

    public BigDecimal getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(BigDecimal refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public Integer getDaysBeforeCheckIn() {
        return daysBeforeCheckIn;
    }

    public void setDaysBeforeCheckIn(Integer daysBeforeCheckIn) {
        this.daysBeforeCheckIn = daysBeforeCheckIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Helper methods
    public boolean isRefundable() {
        return refundPercentage != null && refundPercentage.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean allowsFullRefund() {
        return refundPercentage != null && refundPercentage.compareTo(new BigDecimal("100")) >= 0;
    }

    public boolean isFlexible() {
        return policyType == CancellationPolicyType.FLEXIBLE;
    }

    public boolean isStrict() {
        return policyType != null && policyType.isStrict();
    }

    public boolean hasCustomTerms() {
        if (policyType == null) {
            return false;
        }
        
        // Check if the values differ from the default policy type values
        boolean customRefund = refundPercentage != null && 
                              !refundPercentage.equals(policyType.getDefaultRefundPercentage());
        boolean customDays = daysBeforeCheckIn != null && 
                            !daysBeforeCheckIn.equals(policyType.getDefaultDaysBeforeCheckin());
        boolean customDescription = description != null && 
                                   !description.equals(policyType.getDescription());
        
        return customRefund || customDays || customDescription;
    }

    public String getEffectiveDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        
        if (policyType != null) {
            return policyType.getDescription();
        }
        
        return "Custom cancellation policy";
    }

    public BigDecimal calculateRefundAmount(BigDecimal totalAmount) {
        if (totalAmount == null || !isRefundable()) {
            return BigDecimal.ZERO;
        }
        
        return totalAmount.multiply(refundPercentage.divide(new BigDecimal("100")));
    }

    public String getPolicyDisplayName() {
        if (policyType == null) {
            return "Custom Policy";
        }
        return policyType.getDisplayName();
    }

    public String getRefundInfo() {
        if (!isRefundable()) {
            return "No refund available";
        }
        
        if (allowsFullRefund()) {
            return "Full refund available";
        }
        
        return String.format("%.0f%% refund available", refundPercentage);
    }

    public String getCancellationDeadline() {
        if (daysBeforeCheckIn == null || daysBeforeCheckIn <= 0) {
            return "No cancellation allowed";
        }
        
        if (daysBeforeCheckIn == 1) {
            return "Cancel at least 1 day before check-in";
        }
        
        return String.format("Cancel at least %d days before check-in", daysBeforeCheckIn);
    }

    // Validation methods
    @AssertTrue(message = "Non-refundable policy must have 0% refund percentage")
    public boolean isNonRefundablePolicyValid() {
        if (policyType == CancellationPolicyType.NON_REFUNDABLE) {
            return refundPercentage != null && refundPercentage.compareTo(BigDecimal.ZERO) == 0;
        }
        return true;
    }

    @AssertTrue(message = "Flexible policy should allow reasonable refund percentage")
    public boolean isFlexiblePolicyValid() {
        if (policyType == CancellationPolicyType.FLEXIBLE) {
            return refundPercentage != null && refundPercentage.compareTo(new BigDecimal("50")) >= 0;
        }
        return true;
    }

    @AssertTrue(message = "Strict policies should have limited refund percentage")
    public boolean isStrictPolicyValid() {
        if (policyType == CancellationPolicyType.STRICT || 
            policyType == CancellationPolicyType.SUPER_STRICT_30 || 
            policyType == CancellationPolicyType.SUPER_STRICT_60) {
            return refundPercentage != null && refundPercentage.compareTo(new BigDecimal("50")) <= 0;
        }
        return true;
    }

    @AssertTrue(message = "Days before check-in should be reasonable for the policy type")
    public boolean isDaysBeforeCheckInValid() {
        if (policyType == null || daysBeforeCheckIn == null) {
            return true;
        }
        
        switch (policyType) {
            case FLEXIBLE:
                return daysBeforeCheckIn <= 7; // Flexible should allow short notice
            case MODERATE:
                return daysBeforeCheckIn <= 14; // Moderate should be reasonable
            case STRICT:
                return daysBeforeCheckIn >= 7; // Strict should require advance notice
            case SUPER_STRICT_30:
            case SUPER_STRICT_60:
                return daysBeforeCheckIn >= 14; // Super strict should require significant advance notice
            case NON_REFUNDABLE:
                return true; // No restriction for non-refundable
            default:
                return true;
        }
    }
}
