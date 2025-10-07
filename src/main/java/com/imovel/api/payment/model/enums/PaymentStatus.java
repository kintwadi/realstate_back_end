package com.imovel.api.payment.model.enums;

public enum PaymentStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    SUCCEEDED("succeeded"),
    FAILED("failed"),
    CANCELLED("cancelled"),
    REFUNDED("refunded"),
    PARTIALLY_REFUNDED("partially_refunded"),
    DISPUTED("disputed"),
    EXPIRED("expired");
    
    private final String value;
    
    PaymentStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static PaymentStatus fromString(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + value);
    }
}
