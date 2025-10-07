package com.imovel.api.booking.model.enums;

public enum PaymentStatus {
    PENDING("Payment pending"),
    PROCESSING("Payment processing"),
    COMPLETED("Payment completed"),
    FAILED("Payment failed"),
    CANCELLED("Payment cancelled"),
    REFUNDED("Payment refunded"),
    PARTIALLY_REFUNDED("Payment partially refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REFUNDED;
    }

    public boolean canBeRefunded() {
        return this == COMPLETED;
    }
}
