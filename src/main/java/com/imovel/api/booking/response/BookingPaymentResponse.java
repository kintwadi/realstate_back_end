package com.imovel.api.booking.response;

import com.imovel.api.booking.model.enums.PaymentStatus;
import com.imovel.api.booking.model.enums.PaymentType;
import com.imovel.api.booking.model.BookingPayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingPaymentResponse {

    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionId;
    private String gatewayTransactionId;
    private LocalDateTime paymentDate;
    private LocalDateTime dueDate;
    private BigDecimal refundAmount;
    private LocalDateTime refundDate;
    private String refundTransactionId;
    private String failureReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public BookingPaymentResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }

    public String getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    // Helper methods
    public boolean isSuccessful() {
        return status != null && status.isSuccessful();
    }

    public boolean isFinal() {
        return status != null && status.isFinal();
    }

    public boolean isRefundable() {
        return status != null && status.canBeRefunded() && 
               paymentType != null && paymentType.isRefundable();
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isRefunded() {
        return refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isPartiallyRefunded() {
        return isRefunded() && refundAmount.compareTo(amount) < 0;
    }

    public boolean isFullyRefunded() {
        return isRefunded() && refundAmount.compareTo(amount) >= 0;
    }

    public BigDecimal getRemainingAmount() {
        if (refundAmount == null) {
            return amount;
        }
        return amount.subtract(refundAmount);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && 
               status == PaymentStatus.PENDING;
    }

    public long getDaysUntilDue() {
        if (dueDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }

    public String getStatusDisplayName() {
        if (status == null) {
            return "Unknown";
        }
        return status.getDescription();
    }

    public String getTypeDisplayName() {
        if (paymentType == null) {
            return "Unknown";
        }
        return paymentType.getDescription();
    }

    public static BookingPaymentResponse fromEntity(BookingPayment payment) {
        if (payment == null) {
            return null;
        }
        BookingPaymentResponse resp = new BookingPaymentResponse();
        resp.setId(payment.getId());
        resp.setBookingId(payment.getBooking() != null ? payment.getBooking().getId() : null);
        resp.setAmount(payment.getAmount());
        resp.setPaymentType(payment.getPaymentType());
        resp.setStatus(payment.getPaymentStatus());
        resp.setPaymentMethod(payment.getPaymentMethod());
        resp.setTransactionId(payment.getTransactionId());
        resp.setGatewayTransactionId(payment.getGatewayPaymentId());
        resp.setPaymentDate(payment.getPaymentDate());
        resp.setDueDate(null);
        resp.setRefundAmount(payment.getRefundAmount());
        resp.setRefundDate(payment.getRefundDate());
        resp.setRefundTransactionId(null);
        resp.setFailureReason(payment.getFailureReason());
        resp.setNotes(payment.getNotes());
        resp.setCreatedAt(payment.getCreatedAt());
        resp.setUpdatedAt(payment.getUpdatedAt());
        return resp;
    }
}
