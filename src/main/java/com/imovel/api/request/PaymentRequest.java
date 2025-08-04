package com.imovel.api.request;

import java.math.BigDecimal;

public class PaymentRequest {
    private String name;
    private BigDecimal amount;
    private Long quantity;
    private String currency;

    public PaymentRequest() {}

    public PaymentRequest(String name,BigDecimal amount, Long quantity, String currency) {
        this.amount = amount;
        this.quantity = quantity;
        this.name = name;
        this.currency = currency;
    }

    // Getters and setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}