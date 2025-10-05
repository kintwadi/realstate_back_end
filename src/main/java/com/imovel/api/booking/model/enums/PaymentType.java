package com.imovel.api.booking.model.enums;

public enum PaymentType {
    DEPOSIT("Security deposit"),
    FULL_PAYMENT("Full payment"),
    PARTIAL_PAYMENT("Partial payment"),
    CLEANING_FEE("Cleaning fee"),
    SERVICE_FEE("Service fee"),
    TAX("Tax payment"),
    REFUND("Refund payment"),
    PENALTY("Penalty payment");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRefundable() {
        return this != PENALTY && this != SERVICE_FEE;
    }

    public boolean isRequired() {
        return this == FULL_PAYMENT || this == PARTIAL_PAYMENT || this == DEPOSIT;
    }
}
