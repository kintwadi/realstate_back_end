package com.imovel.api.payment.dto;

import java.math.BigDecimal;

public class PaymentRefundRequest {
    private Long paymentId;
    private BigDecimal refundAmount;
    private String reason;
    private Long userId;

    // Default constructor
    public PaymentRefundRequest() {}

    // All-args constructor
    public PaymentRefundRequest(Long paymentId, BigDecimal refundAmount, String reason, Long userId) {
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.userId = userId;
    }

    // Getters and setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // toString method
    @Override
    public String toString() {
        return "PaymentRefundRequest{" +
                "paymentId=" + paymentId +
                ", refundAmount=" + refundAmount +
                ", reason='" + reason + '\'' +
                ", userId=" + userId +
                '}';
    }
}
