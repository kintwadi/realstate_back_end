package com.imovel.api.payment.model.enums;

public enum PaymentGateway {
    STRIPE("stripe"),
    PAYPAL("paypal"),
    SQUARE("square"),
    RAZORPAY("razorpay");
    
    private final String value;
    
    PaymentGateway(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static PaymentGateway fromString(String value) {
        for (PaymentGateway gateway : PaymentGateway.values()) {
            if (gateway.value.equalsIgnoreCase(value)) {
                return gateway;
            }
        }
        throw new IllegalArgumentException("Unknown payment gateway: " + value);
    }
}