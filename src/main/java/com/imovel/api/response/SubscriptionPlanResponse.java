package com.imovel.api.response;

import com.imovel.api.model.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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

    // Static factory method to parse from SubscriptionPlan entity
    public static SubscriptionPlanResponse parse(SubscriptionPlan subscriptionPlan) {
        SubscriptionPlanResponse response = new SubscriptionPlanResponse();
        response.setId(subscriptionPlan.getId());
        response.setName(subscriptionPlan.getName());
        response.setDescription(subscriptionPlan.getDescription());
        response.setMonthlyPrice(subscriptionPlan.getMonthlyPrice());
        response.setYearlyPrice(subscriptionPlan.getYearlyPrice());
        response.setListingLimit(subscriptionPlan.getListingLimit());
        response.setAvailabilityDays(subscriptionPlan.getAvailabilityDays());
        response.setIsFeatured(subscriptionPlan.getFeatured());
        response.setSupportType(subscriptionPlan.getSupportType());
        return response;
    }
    // Static factory method to parse from List of SubscriptionPlan entities
    public static List<SubscriptionPlanResponse> parse(List<SubscriptionPlan> subscriptionPlans) {
        return subscriptionPlans.stream()
                .map(SubscriptionPlanResponse::parse)
                .collect(Collectors.toList());
    }

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

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean featured) {
        isFeatured = featured;
    }

    public String getSupportType() {
        return supportType;
    }

    public void setSupportType(String supportType) {
        this.supportType = supportType;
    }
}
