package com.imovel.api.booking.response;

import com.imovel.api.booking.model.enums.CancellationPolicyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CancellationPolicyResponse {

    private Long id;
    private Long propertyId;
    private CancellationPolicyType policyType;
    private BigDecimal refundPercentage;
    private Integer daysBeforeCheckIn;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CancellationPolicyResponse() {}

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
    public boolean isRefundable() {
        return policyType != null && policyType.isRefundable();
    }

    public boolean isFlexible() {
        return policyType == CancellationPolicyType.FLEXIBLE;
    }

    public boolean isStrict() {
        return policyType != null && policyType.isStrict();
    }

    public boolean allowsFullRefund() {
        return refundPercentage != null && refundPercentage.compareTo(new BigDecimal("100")) >= 0;
    }

    public boolean allowsPartialRefund() {
        return refundPercentage != null && refundPercentage.compareTo(BigDecimal.ZERO) > 0 &&
               refundPercentage.compareTo(new BigDecimal("100")) < 0;
    }

    public boolean allowsNoRefund() {
        return refundPercentage == null || refundPercentage.compareTo(BigDecimal.ZERO) <= 0;
    }

    public String getPolicyDisplayName() {
        if (policyType == null) {
            return "Unknown Policy";
        }
        return policyType.getDisplayName();
    }

    public String getPolicyDescription() {
        if (policyType == null) {
            return description != null ? description : "No description available";
        }
        return policyType.getDescription();
    }

    public String getRefundInfo() {
        if (allowsNoRefund()) {
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

    public BigDecimal calculateRefundAmount(BigDecimal totalAmount, int daysBeforeCancellation) {
        if (totalAmount == null || !isRefundable() || !isActive) {
            return BigDecimal.ZERO;
        }

        if (daysBeforeCheckIn != null && daysBeforeCancellation < daysBeforeCheckIn) {
            return BigDecimal.ZERO;
        }

        if (refundPercentage == null) {
            return BigDecimal.ZERO;
        }

        return totalAmount.multiply(refundPercentage.divide(new BigDecimal("100")));
    }

    public boolean isEligibleForRefund(int daysBeforeCancellation) {
        if (!isRefundable() || !isActive) {
            return false;
        }
        return daysBeforeCheckIn == null || daysBeforeCancellation >= daysBeforeCheckIn;
    }

    public String getStrictnessLevel() {
        if (policyType == null) {
            return "Unknown";
        }
        
        switch (policyType) {
            case FLEXIBLE:
                return "Low";
            case MODERATE:
                return "Medium";
            case STRICT:
                return "High";
            case SUPER_STRICT_30:
            case SUPER_STRICT_60:
                return "Very High";
            case NON_REFUNDABLE:
                return "Maximum";
            default:
                return "Unknown";
        }
    }
}
