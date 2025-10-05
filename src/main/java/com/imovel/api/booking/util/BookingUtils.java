package com.imovel.api.booking.util;

import com.imovel.api.booking.config.BookingConfiguration;
import com.imovel.api.booking.model.Booking;
import com.imovel.api.booking.model.enums.BookingStatus;
import com.imovel.api.booking.model.CancellationPolicy;
import com.imovel.api.booking.model.enums.CancellationPolicyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for booking-related operations and calculations.
 */
@Component
public class BookingUtils {

    private final BookingConfiguration bookingConfig;

    public BookingUtils(BookingConfiguration bookingConfig) {
        this.bookingConfig = bookingConfig;
    }

    /**
     * Calculate the number of nights between check-in and check-out dates
     */
    public static long calculateNights(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null");
        }
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * Calculate total booking amount including base price, fees, and taxes
     */
    public BigDecimal calculateTotalAmount(BigDecimal baseAmount, BigDecimal serviceFee, 
                                         BigDecimal cleaningFee, BigDecimal taxes) {
        BigDecimal total = baseAmount != null ? baseAmount : BigDecimal.ZERO;
        
        if (serviceFee != null) {
            total = total.add(serviceFee);
        }
        if (cleaningFee != null) {
            total = total.add(cleaningFee);
        }
        if (taxes != null) {
            total = total.add(taxes);
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate service fee based on base amount and percentage
     */
    public BigDecimal calculateServiceFee(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        return baseAmount.multiply(bookingConfig.getDefaultServiceFeePercentage())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate security deposit based on total amount
     */
    public BigDecimal calculateSecurityDeposit(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return bookingConfig.getMinSecurityDeposit();
        }
        
        BigDecimal calculatedDeposit = totalAmount.multiply(bookingConfig.getSecurityDepositPercentage())
                .setScale(2, RoundingMode.HALF_UP);
        
        // Ensure deposit is within min/max bounds
        if (calculatedDeposit.compareTo(bookingConfig.getMinSecurityDeposit()) < 0) {
            return bookingConfig.getMinSecurityDeposit();
        }
        if (calculatedDeposit.compareTo(bookingConfig.getMaxSecurityDeposit()) > 0) {
            return bookingConfig.getMaxSecurityDeposit();
        }
        
        return calculatedDeposit;
    }

    /**
     * Calculate refund amount based on cancellation policy
     */
    public BigDecimal calculateRefundAmount(CancellationPolicy policy, BigDecimal totalAmount, 
                                          LocalDateTime checkInDate) {
        if (policy == null || totalAmount == null || checkInDate == null) {
            return BigDecimal.ZERO;
        }
        
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDateTime.now(), checkInDate);
        
        // If cancellation is after the policy deadline, no refund
        if (daysUntilCheckIn < policy.getDaysBeforeCheckin()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate refund based on policy percentage
        BigDecimal refundPercentage = policy.getRefundPercentage().divide(new BigDecimal("100"));
        return totalAmount.multiply(refundPercentage).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Check if a booking can be cancelled based on policy and timing
     */
    public boolean canCancelBooking(Booking booking, CancellationPolicy policy) {
        if (booking == null || policy == null) {
            return false;
        }
        
        // Cannot cancel if booking is already cancelled or completed
        if (booking.getStatus() == BookingStatus.CANCELLED || 
            booking.getStatus() == BookingStatus.COMPLETED) {
            return false;
        }
        
        // Check if we're within the cancellation deadline
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDateTime.now(), booking.getCheckInDate());
        return daysUntilCheckIn >= policy.getDaysBeforeCheckin();
    }

    /**
     * Check if a booking can be modified
     */
    public boolean canModifyBooking(Booking booking) {
        if (booking == null) {
            return false;
        }
        
        // Cannot modify if booking is cancelled or completed
        if (booking.getStatus() == BookingStatus.CANCELLED || 
            booking.getStatus() == BookingStatus.COMPLETED) {
            return false;
        }
        
        // Check if modifications are enabled
        if (!bookingConfig.getEnableBookingModifications()) {
            return false;
        }
        
        // Check if we're within the modification deadline
        long hoursUntilCheckIn = ChronoUnit.HOURS.between(LocalDateTime.now(), booking.getCheckInDate());
        return hoursUntilCheckIn >= bookingConfig.getModificationDeadlineHours();
    }

    /**
     * Check if a booking qualifies for instant booking
     */
    public boolean qualifiesForInstantBooking(BigDecimal totalAmount) {
        if (!bookingConfig.getEnableInstantBooking()) {
            return false;
        }
        
        return totalAmount != null && 
               totalAmount.compareTo(bookingConfig.getAutoApprovalThreshold()) <= 0;
    }

    /**
     * Generate a list of dates between check-in and check-out (exclusive of check-out)
     */
    public static List<LocalDate> getBookingDates(LocalDate checkInDate, LocalDate checkOutDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = checkInDate;
        
        while (currentDate.isBefore(checkOutDate)) {
            dates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        
        return dates;
    }

    /**
     * Check if booking dates are valid
     */
    public boolean areBookingDatesValid(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return false;
        }
        
        // Check-out must be after check-in
        if (!checkOutDate.isAfter(checkInDate)) {
            return false;
        }
        
        // Check minimum and maximum nights
        long nights = calculateNights(checkInDate, checkOutDate);
        if (nights < bookingConfig.getMinBookingNights() || 
            nights > bookingConfig.getMaxBookingNights()) {
            return false;
        }
        
        // Check advance booking limits
        long daysFromNow = ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);
        if (daysFromNow < bookingConfig.getMinAdvanceBookingDays() || 
            daysFromNow > bookingConfig.getMaxAdvanceBookingDays()) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if guest count is valid
     */
    public boolean isGuestCountValid(int guestCount) {
        return guestCount > 0 && guestCount <= bookingConfig.getMaxGuestsPerBooking();
    }

    /**
     * Calculate early check-in fee
     */
    public BigDecimal calculateEarlyCheckInFee(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        return baseAmount.multiply(bookingConfig.getEarlyCheckInFeePercentage())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate late check-out fee
     */
    public BigDecimal calculateLateCheckOutFee(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        return baseAmount.multiply(bookingConfig.getLateCheckOutFeePercentage())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get default check-in datetime for a given date
     */
    public LocalDateTime getDefaultCheckInDateTime(LocalDate date) {
        LocalTime checkInTime = LocalTime.parse(bookingConfig.getDefaultCheckInTime());
        return LocalDateTime.of(date, checkInTime);
    }

    /**
     * Get default check-out datetime for a given date
     */
    public LocalDateTime getDefaultCheckOutDateTime(LocalDate date) {
        LocalTime checkOutTime = LocalTime.parse(bookingConfig.getDefaultCheckOutTime());
        return LocalDateTime.of(date, checkOutTime);
    }

    /**
     * Check if a booking is overdue for payment
     */
    public boolean isPaymentOverdue(Booking booking) {
        if (booking == null || booking.getCreatedAt() == null) {
            return false;
        }
        
        LocalDateTime paymentDeadline = booking.getCreatedAt()
                .plus(bookingConfig.getPaymentTimeout());
        
        return LocalDateTime.now().isAfter(paymentDeadline) && 
               booking.getStatus() == BookingStatus.PENDING;
    }

    /**
     * Check if a booking confirmation has expired
     */
    public boolean isConfirmationExpired(Booking booking) {
        if (booking == null || booking.getCreatedAt() == null) {
            return false;
        }
        
        LocalDateTime confirmationDeadline = booking.getCreatedAt()
                .plus(bookingConfig.getBookingConfirmationTimeout());
        
        return LocalDateTime.now().isAfter(confirmationDeadline) && 
               booking.getStatus() == BookingStatus.PENDING;
    }

    /**
     * Generate booking reference number
     */
    public static String generateBookingReference(Long bookingId) {
        if (bookingId == null) {
            return null;
        }
        
        // Format: BK-YYYYMMDD-ID (e.g., BK-20240115-12345)
        String dateStr = LocalDate.now().toString().replace("-", "");
        return String.format("BK-%s-%d", dateStr, bookingId);
    }

    /**
     * Check if dates overlap
     */
    public static boolean datesOverlap(LocalDate start1, LocalDate end1, 
                                     LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Get cancellation policy strictness level
     */
    public static String getPolicyStrictnessLevel(CancellationPolicy policy) {
        if (policy == null) {
            return "Unknown";
        }
        CancellationPolicyType type = policy.getPolicyType();
        if (type == CancellationPolicyType.FLEXIBLE) {
            return "Flexible";
        } else if (type == CancellationPolicyType.MODERATE) {
            return "Moderate";
        } else if (type == CancellationPolicyType.STRICT) {
            return "Strict";
        } else if (type == CancellationPolicyType.SUPER_STRICT_30 || type == CancellationPolicyType.SUPER_STRICT_60) {
            return "Super Strict";
        } else if (type == CancellationPolicyType.NON_REFUNDABLE) {
            return "Non-refundable";
        } else {
            return "Custom";
        }
    }

    /**
     * Format currency amount
     */
    public String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        
        return String.format("%.2f %s", amount, bookingConfig.getDefaultCurrency());
    }

    /**
     * Check if booking is eligible for review
     */
    public boolean isEligibleForReview(Booking booking) {
        if (booking == null || !bookingConfig.getEnableGuestReviews()) {
            return false;
        }
        
        // Must be completed booking
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            return false;
        }
        
        // Must be within review deadline
        if (booking.getCheckOutDate() == null) {
            return false;
        }
        
        LocalDateTime reviewDeadline = booking.getCheckOutDate()
                .atStartOfDay()
                .plus(bookingConfig.getReviewDeadline());
        
        return LocalDateTime.now().isBefore(reviewDeadline);
    }
}
