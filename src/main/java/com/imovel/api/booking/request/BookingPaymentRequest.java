package com.imovel.api.booking.request;

import com.imovel.api.booking.model.enums.PaymentType;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingPaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;

    @Size(max = 100, message = "Gateway transaction ID must not exceed 100 characters")
    private String gatewayTransactionId;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Payment gateway specific fields
    @Size(max = 100, message = "Card token must not exceed 100 characters")
    private String cardToken;

    @Size(max = 50, message = "Currency must not exceed 50 characters")
    private String currency;

    @Size(max = 200, message = "Return URL must not exceed 200 characters")
    private String returnUrl;

    @Size(max = 200, message = "Cancel URL must not exceed 200 characters")
    private String cancelUrl;

    @Size(max = 500, message = "Payment description must not exceed 500 characters")
    private String paymentDescription;

    // Constructors
    public BookingPaymentRequest() {}

    // Getters and Setters
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = cardToken;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getPaymentDescription() {
        return paymentDescription;
    }

    public void setPaymentDescription(String paymentDescription) {
        this.paymentDescription = paymentDescription;
    }

    // Helper methods
    public boolean isFullPayment() {
        return paymentType == PaymentType.FULL_PAYMENT;
    }

    public boolean isPartialPayment() {
        return paymentType == PaymentType.PARTIAL_PAYMENT;
    }

    public boolean isDeposit() {
        return paymentType == PaymentType.DEPOSIT;
    }

    public boolean isRefund() {
        return paymentType == PaymentType.REFUND;
    }

    public boolean hasCardToken() {
        return cardToken != null && !cardToken.trim().isEmpty();
    }

    public boolean hasReturnUrl() {
        return returnUrl != null && !returnUrl.trim().isEmpty();
    }

    public boolean hasCancelUrl() {
        return cancelUrl != null && !cancelUrl.trim().isEmpty();
    }

    public boolean isScheduledPayment() {
        return dueDate != null && dueDate.isAfter(LocalDateTime.now());
    }

    public boolean isImmediatePayment() {
        return dueDate == null || !dueDate.isAfter(LocalDateTime.now());
    }

    public String getEffectiveCurrency() {
        return currency != null && !currency.trim().isEmpty() ? currency : "USD";
    }

    public String getEffectiveDescription() {
        if (paymentDescription != null && !paymentDescription.trim().isEmpty()) {
            return paymentDescription;
        }
        
        if (paymentType != null) {
            return paymentType.getDescription() + " for booking #" + bookingId;
        }
        
        return "Payment for booking #" + bookingId;
    }

    // Validation methods
    @AssertTrue(message = "Card token is required for card payments")
    public boolean isCardTokenValidForCardPayments() {
        if (paymentMethod == null) {
            return true;
        }
        
        String method = paymentMethod.toLowerCase();
        if (method.contains("card") || method.contains("credit") || method.contains("debit")) {
            return hasCardToken();
        }
        
        return true;
    }

    @AssertTrue(message = "Return URL is required for online payments")
    public boolean isReturnUrlValidForOnlinePayments() {
        if (paymentMethod == null) {
            return true;
        }
        
        String method = paymentMethod.toLowerCase();
        if (method.contains("paypal") || method.contains("stripe") || method.contains("online")) {
            return hasReturnUrl();
        }
        
        return true;
    }

    @AssertTrue(message = "Refund amount cannot exceed original payment amount")
    public boolean isRefundAmountValid() {
        // This validation would need to be implemented in the service layer
        // where we have access to the original booking amount
        return true;
    }
}
