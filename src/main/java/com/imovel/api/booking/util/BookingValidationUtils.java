package com.imovel.api.booking.util;

import com.imovel.api.booking.config.BookingConfiguration;
import com.imovel.api.booking.model.enums.CancellationPolicyType;
import com.imovel.api.booking.model.enums.BookingStatus;
import com.imovel.api.booking.request.BookingCreateRequest;
import com.imovel.api.booking.request.BookingGuestRequest;
import com.imovel.api.booking.request.CancellationPolicyRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for booking validation operations.
 */
@Component
public class BookingValidationUtils {

    private final BookingConfiguration bookingConfig;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Phone validation pattern (international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );

    public BookingValidationUtils(BookingConfiguration bookingConfig) {
        this.bookingConfig = bookingConfig;
    }

    /**
     * Validate booking request
     */
    public ValidationResult validateBookingRequest(BookingCreateRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request == null) {
            errors.add("Booking request cannot be null");
            return new ValidationResult(false, errors);
        }
        
        // Validate property ID
        if (request.getPropertyId() == null) {
            errors.add("Property ID is required");
        }
        
        // Validate dates
        ValidationResult dateValidation = validateBookingDates(
            request.getCheckInDate(), request.getCheckOutDate());
        if (!dateValidation.isValid()) {
            errors.addAll(dateValidation.getErrors());
        }
        
        // Validate guest counts
        ValidationResult guestValidation = validateGuestCounts(
            request.getNumberOfAdults(), request.getNumberOfChildren(), null);
        if (!guestValidation.isValid()) {
            errors.addAll(guestValidation.getErrors());
        }
        
        // Validate amounts
        if (request.getExpectedTotalAmount() != null && 
            request.getExpectedTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Total amount must be greater than zero");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate booking dates
     */
    public ValidationResult validateBookingDates(LocalDate checkInDate, LocalDate checkOutDate) {
        List<String> errors = new ArrayList<>();
        
        if (checkInDate == null) {
            errors.add("Check-in date is required");
        }
        
        if (checkOutDate == null) {
            errors.add("Check-out date is required");
        }
        
        if (checkInDate != null && checkOutDate != null) {
            // Check-out must be after check-in
            if (!checkOutDate.isAfter(checkInDate)) {
                errors.add("Check-out date must be after check-in date");
            }
            
            // Check minimum and maximum nights
            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            if (nights < bookingConfig.getMinBookingNights()) {
                errors.add(String.format("Minimum stay is %d night(s)", 
                    bookingConfig.getMinBookingNights()));
            }
            if (nights > bookingConfig.getMaxBookingNights()) {
                errors.add(String.format("Maximum stay is %d night(s)", 
                    bookingConfig.getMaxBookingNights()));
            }
            
            // Check advance booking limits
            long daysFromNow = ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);
            if (daysFromNow < bookingConfig.getMinAdvanceBookingDays()) {
                errors.add(String.format("Booking must be made at least %d day(s) in advance", 
                    bookingConfig.getMinAdvanceBookingDays()));
            }
            if (daysFromNow > bookingConfig.getMaxAdvanceBookingDays()) {
                errors.add(String.format("Booking cannot be made more than %d day(s) in advance", 
                    bookingConfig.getMaxAdvanceBookingDays()));
            }
            
            // Check if check-in date is in the past
            if (checkInDate.isBefore(LocalDate.now())) {
                errors.add("Check-in date cannot be in the past");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate guest counts
     */
    public ValidationResult validateGuestCounts(Integer adults, Integer children, Integer infants) {
        List<String> errors = new ArrayList<>();
        
        if (adults == null || adults <= 0) {
            errors.add("At least one adult is required");
        }
        
        if (children != null && children < 0) {
            errors.add("Number of children cannot be negative");
        }
        
        if (infants != null && infants < 0) {
            errors.add("Number of infants cannot be negative");
        }
        
        // Calculate total guests
        int totalGuests = (adults != null ? adults : 0) + 
                         (children != null ? children : 0) + 
                         (infants != null ? infants : 0);
        
        if (totalGuests > bookingConfig.getMaxGuestsPerBooking()) {
            errors.add(String.format("Maximum %d guests allowed per booking", 
                bookingConfig.getMaxGuestsPerBooking()));
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate guest information
     */
    public ValidationResult validateGuestRequest(BookingGuestRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request == null) {
            errors.add("Guest request cannot be null");
            return new ValidationResult(false, errors);
        }
        
        // Validate booking ID
        if (request.getBookingId() == null) {
            errors.add("Booking ID is required");
        }
        
        // Validate name
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            errors.add("First name is required");
        } else if (request.getFirstName().length() > 50) {
            errors.add("First name cannot exceed 50 characters");
        }
        
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            errors.add("Last name is required");
        } else if (request.getLastName().length() > 50) {
            errors.add("Last name cannot exceed 50 characters");
        }
        
        // Validate email
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                errors.add("Invalid email format");
            }
        }
        
        // Validate phone
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                errors.add("Invalid phone number format");
            }
        }
        
        // Validate age
        if (request.getAge() != null && (request.getAge() < 0 || request.getAge() > 150)) {
            errors.add("Age must be between 0 and 150");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate cancellation policy
     */
    public ValidationResult validateCancellationPolicy(CancellationPolicyRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request == null) {
            errors.add("Cancellation policy request cannot be null");
            return new ValidationResult(false, errors);
        }
        
        // Validate property ID
        if (request.getPropertyId() == null) {
            errors.add("Property ID is required");
        }
        
        // Validate policy type
        if (request.getPolicyType() == null) {
            errors.add("Policy type is required");
        }
        
        // Validate refund percentage
        if (request.getRefundPercentage() == null) {
            errors.add("Refund percentage is required");
        } else if (request.getRefundPercentage().compareTo(BigDecimal.ZERO) < 0 || 
                   request.getRefundPercentage().compareTo(new BigDecimal("100")) > 0) {
            errors.add("Refund percentage must be between 0 and 100");
        }
        
        // Validate days before check-in
        if (request.getDaysBeforeCheckIn() == null) {
            errors.add("Days before check-in is required");
        } else if (request.getDaysBeforeCheckIn() < 0) {
            errors.add("Days before check-in cannot be negative");
        }
        
        // Validate policy consistency
        if (request.getPolicyType() != null && request.getRefundPercentage() != null && 
            request.getDaysBeforeCheckIn() != null) {
            ValidationResult consistencyValidation = validatePolicyConsistency(
                request.getPolicyType(), request.getRefundPercentage(), request.getDaysBeforeCheckIn());
            if (!consistencyValidation.isValid()) {
                errors.addAll(consistencyValidation.getErrors());
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate policy consistency with type
     */
    public ValidationResult validatePolicyConsistency(CancellationPolicyType policyType, 
                                                    BigDecimal refundPercentage, 
                                                    Integer daysBeforeCheckIn) {
        List<String> errors = new ArrayList<>();
        
        switch (policyType) {
            case FLEXIBLE:
                if (refundPercentage.compareTo(new BigDecimal("80")) < 0) {
                    errors.add("Flexible policy should offer at least 80% refund");
                }
                if (daysBeforeCheckIn > 1) {
                    errors.add("Flexible policy should allow cancellation up to 1 day before check-in");
                }
                break;
                
            case MODERATE:
                if (refundPercentage.compareTo(new BigDecimal("50")) < 0) {
                    errors.add("Moderate policy should offer at least 50% refund");
                }
                if (daysBeforeCheckIn > 5) {
                    errors.add("Moderate policy should allow cancellation up to 5 days before check-in");
                }
                break;
                
            case STRICT:
                if (refundPercentage.compareTo(new BigDecimal("50")) > 0) {
                    errors.add("Strict policy should offer maximum 50% refund");
                }
                if (daysBeforeCheckIn < 7) {
                    errors.add("Strict policy should require at least 7 days notice");
                }
                break;
                
            case SUPER_STRICT_30:
                if (refundPercentage.compareTo(new BigDecimal("50")) > 0) {
                    errors.add("Super strict 30 policy should offer maximum 50% refund");
                }
                if (daysBeforeCheckIn < 30) {
                    errors.add("Super strict 30 policy should require at least 30 days notice");
                }
                break;
                
            case SUPER_STRICT_60:
                if (refundPercentage.compareTo(new BigDecimal("50")) > 0) {
                    errors.add("Super strict 60 policy should offer maximum 50% refund");
                }
                if (daysBeforeCheckIn < 60) {
                    errors.add("Super strict 60 policy should require at least 60 days notice");
                }
                break;
                
            case NON_REFUNDABLE:
                if (refundPercentage.compareTo(BigDecimal.ZERO) > 0) {
                    errors.add("Non-refundable policy should offer 0% refund");
                }
                break;
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate payment amount
     */
    public ValidationResult validatePaymentAmount(BigDecimal amount, BigDecimal bookingTotal) {
        List<String> errors = new ArrayList<>();
        
        if (amount == null) {
            errors.add("Payment amount is required");
            return new ValidationResult(false, errors);
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Payment amount must be greater than zero");
        }
        
        if (bookingTotal != null && amount.compareTo(bookingTotal) > 0) {
            errors.add("Payment amount cannot exceed booking total");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate booking status transition
     */
    public ValidationResult validateStatusTransition(BookingStatus currentStatus, 
                                                   BookingStatus newStatus) {
        List<String> errors = new ArrayList<>();
        
        if (currentStatus == null || newStatus == null) {
            errors.add("Current and new status are required");
            return new ValidationResult(false, errors);
        }
        
        // Define valid transitions
        boolean isValidTransition = false;
        
        switch (currentStatus) {
            case PENDING:
                isValidTransition = newStatus == BookingStatus.CONFIRMED || 
                                  newStatus == BookingStatus.CANCELLED;
                break;
                
            case CONFIRMED:
                isValidTransition = newStatus == BookingStatus.CHECKED_IN || 
                                  newStatus == BookingStatus.CANCELLED;
                break;
                
            case CHECKED_IN:
                isValidTransition = newStatus == BookingStatus.CHECKED_OUT;
                break;
                
            case CHECKED_OUT:
                isValidTransition = newStatus == BookingStatus.COMPLETED;
                break;
                
            case COMPLETED:
            case CANCELLED:
                // Terminal states - no transitions allowed
                isValidTransition = false;
                break;
        }
        
        if (!isValidTransition) {
            errors.add(String.format("Invalid status transition from %s to %s", 
                currentStatus, newStatus));
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate property capacity against guest count
     */
    public ValidationResult validatePropertyCapacity(int guestCount, int propertyCapacity) {
        List<String> errors = new ArrayList<>();
        
        if (guestCount > propertyCapacity) {
            errors.add(String.format("Guest count (%d) exceeds property capacity (%d)", 
                guestCount, propertyCapacity));
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
