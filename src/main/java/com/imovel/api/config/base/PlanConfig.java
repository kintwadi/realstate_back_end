package com.imovel.api.config.base;

import java.math.BigDecimal;

public class PlanConfig {
        private String name;
        private String description;
        private BigDecimal monthlyPrice;
        private BigDecimal yearlyPrice;
        private Integer listingLimit;
        private Integer availabilityDays;
        private Boolean featured;
        private String supportType;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getMonthlyPrice() { return monthlyPrice; }
        public void setMonthlyPrice(BigDecimal monthlyPrice) { this.monthlyPrice = monthlyPrice; }

        public BigDecimal getYearlyPrice() { return yearlyPrice; }
        public void setYearlyPrice(BigDecimal yearlyPrice) { this.yearlyPrice = yearlyPrice; }

        public Integer getListingLimit() { return listingLimit; }
        public void setListingLimit(Integer listingLimit) { this.listingLimit = listingLimit; }

        public Integer getAvailabilityDays() { return availabilityDays; }
        public void setAvailabilityDays(Integer availabilityDays) { this.availabilityDays = availabilityDays; }

        public Boolean getFeatured() { return featured; }
        public void setFeatured(Boolean featured) { this.featured = featured; }

        public String getSupportType() { return supportType; }
        public void setSupportType(String supportType) { this.supportType = supportType; }
    }