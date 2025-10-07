package com.imovel.api.booking.model;

import com.imovel.api.booking.model.enums.PaymentStatus;
import com.imovel.api.booking.model.enums.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "booking_payments")
public class BookingPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull
    @Positive
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @Column(name = "gateway_payment_id", length = 255)
    private String gatewayPaymentId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "refund_amount", precision = 19, scale = 4)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public BookingPayment() {}

    public BookingPayment(Booking booking, BigDecimal amount, PaymentType paymentType) {
        this.booking = booking;
        this.amount = amount;
        this.paymentType = paymentType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
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

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }

    public void setGatewayPaymentId(String gatewayPaymentId) {
        this.gatewayPaymentId = gatewayPaymentId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
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

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
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

    // Business methods
    public void markAsCompleted(String transactionId, String gatewayPaymentId) {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.gatewayPaymentId = gatewayPaymentId;
        this.paymentDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void processRefund(BigDecimal refundAmount, String reason) {
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.refundAmount = refundAmount;
        this.refundReason = reason;
        this.refundDate = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    public boolean canBeRefunded() {
        return paymentStatus == PaymentStatus.COMPLETED && 
               (refundAmount == null || refundAmount.compareTo(amount) < 0);
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingPayment that = (BookingPayment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BookingPayment{" +
                "id=" + id +
                ", amount=" + amount +
                ", paymentType=" + paymentType +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
