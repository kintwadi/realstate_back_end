package com.imovel.api.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;
    
    @Column(name = "yearly_price", nullable = false)
    private BigDecimal yearlyPrice;
    
    @Column(name = "listing_limit")
    private Integer listingLimit; // null means unlimited
    
    @Column(name = "availability_days")
    private Integer availabilityDays;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "support_type", nullable = false)
    private String supportType; // "limited", "24/7"
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getYearlyPrice() {
        return yearlyPrice;
    }

    public void setYearlyPrice(BigDecimal yearlyPrice) {
        this.yearlyPrice = yearlyPrice;
    }

    public Integer getListingLimit() {
        return listingLimit;
    }

    public void setListingLimit(Integer listingLimit) {
        this.listingLimit = listingLimit;
    }

    public Integer getAvailabilityDays() {
        return availabilityDays;
    }

    public void setAvailabilityDays(Integer availabilityDays) {
        this.availabilityDays = availabilityDays;
    }

    public Boolean getFeatured() {
        return isFeatured;
    }

    public void setFeatured(Boolean featured) {
        isFeatured = featured;
    }

    public String getSupportType() {
        return supportType;
    }

    public void setSupportType(String supportType) {
        this.supportType = supportType;
    }
}