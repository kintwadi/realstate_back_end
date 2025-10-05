package com.imovel.api.payment.model.enums;

public enum PaymentMethod {
    CREDIT_CARD("credit_card"),
    DEBIT_CARD("debit_card"),
    BANK_TRANSFER("bank_transfer"),
    DIGITAL_WALLET("digital_wallet"),
    CASH("cash"),
    CHECK("check"),
    CRYPTOCURRENCY("cryptocurrency");
    
    private final String value;
    
    PaymentMethod(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static PaymentMethod fromString(String value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value.equalsIgnoreCase(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method: " + value);
    }
}
