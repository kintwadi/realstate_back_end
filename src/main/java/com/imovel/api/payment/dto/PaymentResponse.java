package com.imovel.api.payment.dto;

import com.imovel.api.payment.model.enums.PaymentGateway;
import com.imovel.api.payment.model.enums.PaymentMethod;
import com.imovel.api.payment.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private Long quantity;
    private String customerName;
    private PaymentGateway gateway;
    private PaymentMethod method;
    private PaymentStatus status;
    private String gatewayPaymentId;
    private String clientSecret;
    private String description;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public PaymentResponse() {}
    
    public PaymentResponse(Long id, Long userId, BigDecimal amount, String currency, 
                          Long quantity, String customerName, PaymentGateway gateway, 
                          PaymentMethod method, PaymentStatus status, String gatewayPaymentId, String clientSecret,
                          String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.quantity = quantity;
        this.customerName = customerName;
        this.gateway = gateway;
        this.method = method;
        this.status = status;
        this.gatewayPaymentId = gatewayPaymentId;
        this.clientSecret= clientSecret;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Long getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public PaymentGateway getGateway() {
        return gateway;
    }
    
    public void setGateway(PaymentGateway gateway) {
        this.gateway = gateway;
    }
    
    public PaymentMethod getMethod() {
        return method;
    }
    
    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }
    
    public void setGatewayPaymentId(String gatewayPaymentId) {
        this.gatewayPaymentId = gatewayPaymentId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}