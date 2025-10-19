package com.imovel.api.booking.model;

import com.imovel.api.booking.model.enums.CancellationPolicyType;
import com.imovel.api.model.Property;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cancellation_policies")
public class CancellationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type")
    private CancellationPolicyType policyType;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(name = "refund_percentage")
    private BigDecimal refundPercentage;

    @NotNull
    @Min(0)
    @Column(name = "days_before_checkin")
    private Integer daysBeforeCheckin;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CancellationPolicy() {}

    public CancellationPolicy(Property property, CancellationPolicyType policyType, 
                             BigDecimal refundPercentage, Integer daysBeforeCheckin) {
        this.property = property;
        this.policyType = policyType;
        this.refundPercentage = refundPercentage;
        this.daysBeforeCheckin = daysBeforeCheckin;
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

    public Integer getDaysBeforeCheckin() {
        return daysBeforeCheckin;
    }

    public void setDaysBeforeCheckin(Integer daysBeforeCheckin) {
        this.daysBeforeCheckin = daysBeforeCheckin;
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

    // Business methods
    public BigDecimal calculateRefundAmount(BigDecimal totalAmount, long daysUntilCheckin) {
        if (daysUntilCheckin >= daysBeforeCheckin) {
            return totalAmount.multiply(refundPercentage).divide(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    public boolean isRefundEligible(long daysUntilCheckin) {
        return daysUntilCheckin >= daysBeforeCheckin && refundPercentage.compareTo(BigDecimal.ZERO) > 0;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CancellationPolicy that = (CancellationPolicy) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CancellationPolicy{" +
                "id=" + id +
                ", policyType=" + policyType +
                ", refundPercentage=" + refundPercentage +
                ", daysBeforeCheckin=" + daysBeforeCheckin +
                '}';
    }
}
