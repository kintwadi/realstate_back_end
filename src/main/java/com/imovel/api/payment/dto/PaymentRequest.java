package com.imovel.api.payment.dto;

import com.imovel.api.payment.model.enums.PaymentGateway;
import com.imovel.api.payment.model.enums.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PaymentRequest {
    
    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String name;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity = 1L;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;
    
    @NotBlank(message = "Gateway is required")
    private String gateway; // eg: stripe, paypal, etc
    
    @NotBlank(message = "Payment method is required")
    private String method; // eg: credit_card, cash etc, this is only needed for reference, no credit card information needed
    
    private String description;
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(String name, BigDecimal amount, Long quantity, String currency, 
                         String gateway, String method) {
        this.name = name;
        this.amount = amount;
        this.quantity = quantity;
        this.currency = currency;
        this.gateway = gateway;
        this.method = method;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Long getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getGateway() {
        return gateway;
    }
    
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // Convenience methods for enum conversion
    public PaymentGateway getGatewayEnum() {
        return PaymentGateway.fromString(this.gateway);
    }
    
    public PaymentMethod getMethodEnum() {
        return PaymentMethod.fromString(this.method);
    }
}