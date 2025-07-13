package com.imovel.api.response;

import java.math.BigDecimal;

public class SubscriptionPlanResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer listingLimit;
    private Integer availabilityDays;
    private Boolean isFeatured;
    private String supportType;

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