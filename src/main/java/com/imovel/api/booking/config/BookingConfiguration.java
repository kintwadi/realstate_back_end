package com.imovel.api.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;

/**
 * Configuration class for booking-related settings.
 * Contains configurable parameters for booking operations.
 */
@Configuration
@ConfigurationProperties(prefix = "booking")
@Validated
public class BookingConfiguration {

    /**
     * Maximum number of days in advance a booking can be made
     */
    @NotNull
    @Min(1)
    private Integer maxAdvanceBookingDays = 365;

    /**
     * Minimum number of days in advance a booking must be made
     */
    @NotNull
    @Min(0)
    private Integer minAdvanceBookingDays = 0;

    /**
     * Maximum number of nights for a single booking
     */
    @NotNull
    @Min(1)
    private Integer maxBookingNights = 30;

    /**
     * Minimum number of nights for a single booking
     */
    @NotNull
    @Min(1)
    private Integer minBookingNights = 1;

    /**
     * Maximum number of guests allowed per booking
     */
    @NotNull
    @Min(1)
    private Integer maxGuestsPerBooking = 16;

    /**
     * Default cancellation deadline in hours before check-in
     */
    @NotNull
    @Min(0)
    private Integer defaultCancellationDeadlineHours = 24;

    /**
     * Booking confirmation timeout in minutes
     */
    @NotNull
    @Min(1)
    private Integer bookingConfirmationTimeoutMinutes = 30;

    /**
     * Payment processing timeout in minutes
     */
    @NotNull
    @Min(1)
    private Integer paymentTimeoutMinutes = 15;

    /**
     * Default service fee percentage
     */
    @NotNull
    @Min(0)
    private BigDecimal defaultServiceFeePercentage = new BigDecimal("0.03");

    /**
     * Default cleaning fee amount
     */
    @NotNull
    @Min(0)
    private BigDecimal defaultCleaningFee = new BigDecimal("50.00");

    /**
     * Security deposit percentage of total booking amount
     */
    @NotNull
    @Min(0)
    private BigDecimal securityDepositPercentage = new BigDecimal("0.20");

    /**
     * Maximum security deposit amount
     */
    @NotNull
    @Min(0)
    private BigDecimal maxSecurityDeposit = new BigDecimal("1000.00");

    /**
     * Minimum security deposit amount
     */
    @NotNull
    @Min(0)
    private BigDecimal minSecurityDeposit = new BigDecimal("100.00");

    /**
     * Auto-approval threshold amount
     */
    @NotNull
    @Min(0)
    private BigDecimal autoApprovalThreshold = new BigDecimal("500.00");

    /**
     * Enable instant booking by default
     */
    @NotNull
    private Boolean enableInstantBooking = true;

    /**
     * Enable automatic payment processing
     */
    @NotNull
    private Boolean enableAutoPayment = true;

    /**
     * Enable email notifications
     */
    @NotNull
    private Boolean enableEmailNotifications = true;

    /**
     * Enable SMS notifications
     */
    @NotNull
    private Boolean enableSmsNotifications = false;

    /**
     * Refund processing timeout in days
     */
    @NotNull
    @Min(1)
    private Integer refundProcessingDays = 7;

    /**
     * Check-in time (24-hour format)
     */
    @NotNull
    private String defaultCheckInTime = "15:00";

    /**
     * Check-out time (24-hour format)
     */
    @NotNull
    private String defaultCheckOutTime = "11:00";

    /**
     * Grace period for late check-out in minutes
     */
    @NotNull
    @Min(0)
    private Integer checkOutGracePeriodMinutes = 30;

    /**
     * Early check-in fee percentage
     */
    @NotNull
    @Min(0)
    private BigDecimal earlyCheckInFeePercentage = new BigDecimal("0.10");

    /**
     * Late check-out fee percentage
     */
    @NotNull
    @Min(0)
    private BigDecimal lateCheckOutFeePercentage = new BigDecimal("0.15");

    /**
     * Currency code for bookings
     */
    @NotNull
    private String defaultCurrency = "USD";

    /**
     * Time zone for booking operations
     */
    @NotNull
    private String defaultTimeZone = "UTC";

    /**
     * Maximum number of concurrent bookings per user
     */
    @NotNull
    @Min(1)
    private Integer maxConcurrentBookingsPerUser = 5;

    /**
     * Booking modification deadline in hours before check-in
     */
    @NotNull
    @Min(0)
    private Integer modificationDeadlineHours = 48;

    /**
     * Enable booking modifications
     */
    @NotNull
    private Boolean enableBookingModifications = true;

    /**
     * Enable guest reviews
     */
    @NotNull
    private Boolean enableGuestReviews = true;

    /**
     * Review deadline in days after check-out
     */
    @NotNull
    @Min(1)
    private Integer reviewDeadlineDays = 14;

    // Getters and Setters

    public Integer getMaxAdvanceBookingDays() {
        return maxAdvanceBookingDays;
    }

    public void setMaxAdvanceBookingDays(Integer maxAdvanceBookingDays) {
        this.maxAdvanceBookingDays = maxAdvanceBookingDays;
    }

    public Integer getMinAdvanceBookingDays() {
        return minAdvanceBookingDays;
    }

    public void setMinAdvanceBookingDays(Integer minAdvanceBookingDays) {
        this.minAdvanceBookingDays = minAdvanceBookingDays;
    }

    public Integer getMaxBookingNights() {
        return maxBookingNights;
    }

    public void setMaxBookingNights(Integer maxBookingNights) {
        this.maxBookingNights = maxBookingNights;
    }

    public Integer getMinBookingNights() {
        return minBookingNights;
    }

    public void setMinBookingNights(Integer minBookingNights) {
        this.minBookingNights = minBookingNights;
    }

    public Integer getMaxGuestsPerBooking() {
        return maxGuestsPerBooking;
    }

    public void setMaxGuestsPerBooking(Integer maxGuestsPerBooking) {
        this.maxGuestsPerBooking = maxGuestsPerBooking;
    }

    public Integer getDefaultCancellationDeadlineHours() {
        return defaultCancellationDeadlineHours;
    }

    public void setDefaultCancellationDeadlineHours(Integer defaultCancellationDeadlineHours) {
        this.defaultCancellationDeadlineHours = defaultCancellationDeadlineHours;
    }

    public Integer getBookingConfirmationTimeoutMinutes() {
        return bookingConfirmationTimeoutMinutes;
    }

    public void setBookingConfirmationTimeoutMinutes(Integer bookingConfirmationTimeoutMinutes) {
        this.bookingConfirmationTimeoutMinutes = bookingConfirmationTimeoutMinutes;
    }

    public Integer getPaymentTimeoutMinutes() {
        return paymentTimeoutMinutes;
    }

    public void setPaymentTimeoutMinutes(Integer paymentTimeoutMinutes) {
        this.paymentTimeoutMinutes = paymentTimeoutMinutes;
    }

    public BigDecimal getDefaultServiceFeePercentage() {
        return defaultServiceFeePercentage;
    }

    public void setDefaultServiceFeePercentage(BigDecimal defaultServiceFeePercentage) {
        this.defaultServiceFeePercentage = defaultServiceFeePercentage;
    }

    public BigDecimal getDefaultCleaningFee() {
        return defaultCleaningFee;
    }

    public void setDefaultCleaningFee(BigDecimal defaultCleaningFee) {
        this.defaultCleaningFee = defaultCleaningFee;
    }

    public BigDecimal getSecurityDepositPercentage() {
        return securityDepositPercentage;
    }

    public void setSecurityDepositPercentage(BigDecimal securityDepositPercentage) {
        this.securityDepositPercentage = securityDepositPercentage;
    }

    public BigDecimal getMaxSecurityDeposit() {
        return maxSecurityDeposit;
    }

    public void setMaxSecurityDeposit(BigDecimal maxSecurityDeposit) {
        this.maxSecurityDeposit = maxSecurityDeposit;
    }

    public BigDecimal getMinSecurityDeposit() {
        return minSecurityDeposit;
    }

    public void setMinSecurityDeposit(BigDecimal minSecurityDeposit) {
        this.minSecurityDeposit = minSecurityDeposit;
    }

    public BigDecimal getAutoApprovalThreshold() {
        return autoApprovalThreshold;
    }

    public void setAutoApprovalThreshold(BigDecimal autoApprovalThreshold) {
        this.autoApprovalThreshold = autoApprovalThreshold;
    }

    public Boolean getEnableInstantBooking() {
        return enableInstantBooking;
    }

    public void setEnableInstantBooking(Boolean enableInstantBooking) {
        this.enableInstantBooking = enableInstantBooking;
    }

    public Boolean getEnableAutoPayment() {
        return enableAutoPayment;
    }

    public void setEnableAutoPayment(Boolean enableAutoPayment) {
        this.enableAutoPayment = enableAutoPayment;
    }

    public Boolean getEnableEmailNotifications() {
        return enableEmailNotifications;
    }

    public void setEnableEmailNotifications(Boolean enableEmailNotifications) {
        this.enableEmailNotifications = enableEmailNotifications;
    }

    public Boolean getEnableSmsNotifications() {
        return enableSmsNotifications;
    }

    public void setEnableSmsNotifications(Boolean enableSmsNotifications) {
        this.enableSmsNotifications = enableSmsNotifications;
    }

    public Integer getRefundProcessingDays() {
        return refundProcessingDays;
    }

    public void setRefundProcessingDays(Integer refundProcessingDays) {
        this.refundProcessingDays = refundProcessingDays;
    }

    public String getDefaultCheckInTime() {
        return defaultCheckInTime;
    }

    public void setDefaultCheckInTime(String defaultCheckInTime) {
        this.defaultCheckInTime = defaultCheckInTime;
    }

    public String getDefaultCheckOutTime() {
        return defaultCheckOutTime;
    }

    public void setDefaultCheckOutTime(String defaultCheckOutTime) {
        this.defaultCheckOutTime = defaultCheckOutTime;
    }

    public Integer getCheckOutGracePeriodMinutes() {
        return checkOutGracePeriodMinutes;
    }

    public void setCheckOutGracePeriodMinutes(Integer checkOutGracePeriodMinutes) {
        this.checkOutGracePeriodMinutes = checkOutGracePeriodMinutes;
    }

    public BigDecimal getEarlyCheckInFeePercentage() {
        return earlyCheckInFeePercentage;
    }

    public void setEarlyCheckInFeePercentage(BigDecimal earlyCheckInFeePercentage) {
        this.earlyCheckInFeePercentage = earlyCheckInFeePercentage;
    }

    public BigDecimal getLateCheckOutFeePercentage() {
        return lateCheckOutFeePercentage;
    }

    public void setLateCheckOutFeePercentage(BigDecimal lateCheckOutFeePercentage) {
        this.lateCheckOutFeePercentage = lateCheckOutFeePercentage;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    public void setDefaultTimeZone(String defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    public Integer getMaxConcurrentBookingsPerUser() {
        return maxConcurrentBookingsPerUser;
    }

    public void setMaxConcurrentBookingsPerUser(Integer maxConcurrentBookingsPerUser) {
        this.maxConcurrentBookingsPerUser = maxConcurrentBookingsPerUser;
    }

    public Integer getModificationDeadlineHours() {
        return modificationDeadlineHours;
    }

    public void setModificationDeadlineHours(Integer modificationDeadlineHours) {
        this.modificationDeadlineHours = modificationDeadlineHours;
    }

    public Boolean getEnableBookingModifications() {
        return enableBookingModifications;
    }

    public void setEnableBookingModifications(Boolean enableBookingModifications) {
        this.enableBookingModifications = enableBookingModifications;
    }

    public Boolean getEnableGuestReviews() {
        return enableGuestReviews;
    }

    public void setEnableGuestReviews(Boolean enableGuestReviews) {
        this.enableGuestReviews = enableGuestReviews;
    }

    public Integer getReviewDeadlineDays() {
        return reviewDeadlineDays;
    }

    public void setReviewDeadlineDays(Integer reviewDeadlineDays) {
        this.reviewDeadlineDays = reviewDeadlineDays;
    }

    // Utility methods

    /**
     * Get booking confirmation timeout as Duration
     */
    public Duration getBookingConfirmationTimeout() {
        return Duration.ofMinutes(bookingConfirmationTimeoutMinutes);
    }

    /**
     * Get payment timeout as Duration
     */
    public Duration getPaymentTimeout() {
        return Duration.ofMinutes(paymentTimeoutMinutes);
    }

    /**
     * Get cancellation deadline as Duration
     */
    public Duration getDefaultCancellationDeadline() {
        return Duration.ofHours(defaultCancellationDeadlineHours);
    }

    /**
     * Get modification deadline as Duration
     */
    public Duration getModificationDeadline() {
        return Duration.ofHours(modificationDeadlineHours);
    }

    /**
     * Get check-out grace period as Duration
     */
    public Duration getCheckOutGracePeriod() {
        return Duration.ofMinutes(checkOutGracePeriodMinutes);
    }

    /**
     * Get refund processing period as Duration
     */
    public Duration getRefundProcessingPeriod() {
        return Duration.ofDays(refundProcessingDays);
    }

    /**
     * Get review deadline as Duration
     */
    public Duration getReviewDeadline() {
        return Duration.ofDays(reviewDeadlineDays);
    }
}
