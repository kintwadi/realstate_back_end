package com.imovel.api.booking.constants;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Constants used throughout the booking module.
 */
public final class BookingConstants {

    private BookingConstants() {
        // Utility class - prevent instantiation
    }

    // Booking Status Messages
    public static final class StatusMessages {
        public static final String PENDING_CONFIRMATION = "Booking is pending host confirmation";
        public static final String PENDING_PAYMENT = "Booking is pending payment";
        public static final String CONFIRMED = "Booking is confirmed";
        public static final String CHECKED_IN = "Guest has checked in";
        public static final String CHECKED_OUT = "Guest has checked out";
        public static final String COMPLETED = "Booking is completed";
        public static final String CANCELLED = "Booking has been cancelled";
    }

    // Payment Status Messages
    public static final class PaymentMessages {
        public static final String PENDING = "Payment is pending";
        public static final String PROCESSING = "Payment is being processed";
        public static final String COMPLETED = "Payment completed successfully";
        public static final String FAILED = "Payment failed";
        public static final String CANCELLED = "Payment was cancelled";
        public static final String REFUNDED = "Payment has been refunded";
        public static final String PARTIALLY_REFUNDED = "Payment has been partially refunded";
    }

    // Error Messages
    public static final class ErrorMessages {
        public static final String BOOKING_NOT_FOUND = "Booking not found";
        public static final String PROPERTY_NOT_FOUND = "Property not found";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String UNAUTHORIZED_ACCESS = "Unauthorized access to booking";
        public static final String INVALID_BOOKING_STATUS = "Invalid booking status for this operation";
        public static final String PROPERTY_NOT_AVAILABLE = "Property is not available for selected dates";
        public static final String INVALID_DATE_RANGE = "Invalid date range";
        public static final String PAYMENT_REQUIRED = "Payment is required to confirm booking";
        public static final String CANCELLATION_NOT_ALLOWED = "Cancellation is not allowed for this booking";
        public static final String MODIFICATION_NOT_ALLOWED = "Modification is not allowed for this booking";
        public static final String GUEST_LIMIT_EXCEEDED = "Guest limit exceeded for this property";
        public static final String INVALID_GUEST_COUNT = "Invalid guest count";
        public static final String DUPLICATE_BOOKING = "Duplicate booking detected";
        public static final String BOOKING_EXPIRED = "Booking has expired";
        public static final String PAYMENT_TIMEOUT = "Payment timeout exceeded";
        public static final String INSUFFICIENT_FUNDS = "Insufficient funds for payment";
        public static final String REFUND_NOT_ALLOWED = "Refund is not allowed for this booking";
        public static final String INVALID_CANCELLATION_POLICY = "Invalid cancellation policy";
        public static final String GUEST_ALREADY_EXISTS = "Guest already exists for this booking";
        public static final String PRIMARY_GUEST_REQUIRED = "Primary guest is required";
        public static final String CANNOT_REMOVE_PRIMARY_GUEST = "Cannot remove primary guest";
    }

    // Success Messages
    public static final class SuccessMessages {
        public static final String BOOKING_CREATED = "Booking created successfully";
        public static final String BOOKING_UPDATED = "Booking updated successfully";
        public static final String BOOKING_CANCELLED = "Booking cancelled successfully";
        public static final String BOOKING_CONFIRMED = "Booking confirmed successfully";
        public static final String PAYMENT_PROCESSED = "Payment processed successfully";
        public static final String REFUND_PROCESSED = "Refund processed successfully";
        public static final String GUEST_ADDED = "Guest added successfully";
        public static final String GUEST_UPDATED = "Guest updated successfully";
        public static final String GUEST_REMOVED = "Guest removed successfully";
        public static final String AVAILABILITY_UPDATED = "Availability updated successfully";
        public static final String POLICY_CREATED = "Cancellation policy created successfully";
        public static final String POLICY_UPDATED = "Cancellation policy updated successfully";
    }

    // Default Values
    public static final class Defaults {
        public static final LocalTime DEFAULT_CHECK_IN_TIME = LocalTime.of(15, 0); // 3:00 PM
        public static final LocalTime DEFAULT_CHECK_OUT_TIME = LocalTime.of(11, 0); // 11:00 AM
        public static final BigDecimal DEFAULT_SERVICE_FEE_PERCENTAGE = new BigDecimal("0.03"); // 3%
        public static final BigDecimal DEFAULT_SECURITY_DEPOSIT_PERCENTAGE = new BigDecimal("0.20"); // 20%
        public static final BigDecimal DEFAULT_CLEANING_FEE = new BigDecimal("50.00");
        public static final int DEFAULT_MIN_NIGHTS = 1;
        public static final int DEFAULT_MAX_NIGHTS = 30;
        public static final int DEFAULT_MAX_GUESTS = 8;
        public static final int DEFAULT_ADVANCE_BOOKING_DAYS = 365;
        public static final int DEFAULT_MIN_ADVANCE_DAYS = 0;
        public static final int DEFAULT_PAYMENT_TIMEOUT_HOURS = 24;
        public static final int DEFAULT_CANCELLATION_DEADLINE_HOURS = 24;
    }

    // Validation Limits
    public static final class Limits {
        public static final int MAX_BOOKING_REFERENCE_LENGTH = 20;
        public static final int MAX_GUEST_NAME_LENGTH = 50;
        public static final int MAX_SPECIAL_REQUESTS_LENGTH = 1000;
        public static final int MAX_CANCELLATION_REASON_LENGTH = 500;
        public static final int MAX_GUESTS_PER_BOOKING = 16;
        public static final int MIN_GUEST_AGE = 0;
        public static final int MAX_GUEST_AGE = 150;
        public static final BigDecimal MAX_REFUND_PERCENTAGE = new BigDecimal("100");
        public static final BigDecimal MIN_REFUND_PERCENTAGE = BigDecimal.ZERO;
        public static final int MAX_CANCELLATION_DAYS = 365;
        public static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("100000.00");
        public static final BigDecimal MIN_PAYMENT_AMOUNT = new BigDecimal("1.00");
    }

    // Date Formats
    public static final class DateFormats {
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String TIME_FORMAT = "HH:mm";
        public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
        public static final String DISPLAY_DATETIME_FORMAT = "MMM dd, yyyy HH:mm";
    }

    // Currency Formats
    public static final class CurrencyFormats {
        public static final String DEFAULT_CURRENCY = "USD";
        public static final String CURRENCY_SYMBOL = "$";
        public static final int DECIMAL_PLACES = 2;
    }

    // Booking Reference Patterns
    public static final class ReferencePatterns {
        public static final String BOOKING_PREFIX = "BK";
        public static final String PAYMENT_PREFIX = "PAY";
        public static final String REFUND_PREFIX = "REF";
        public static final String REFERENCE_SEPARATOR = "-";
    }

    // Email Templates
    public static final class EmailTemplates {
        public static final String BOOKING_CONFIRMATION = "booking_confirmation";
        public static final String BOOKING_CANCELLATION = "booking_cancellation";
        public static final String PAYMENT_CONFIRMATION = "payment_confirmation";
        public static final String REFUND_NOTIFICATION = "refund_notification";
        public static final String CHECK_IN_REMINDER = "check_in_reminder";
        public static final String CHECK_OUT_REMINDER = "check_out_reminder";
        public static final String BOOKING_MODIFICATION = "booking_modification";
    }

    // Notification Types
    public static final class NotificationTypes {
        public static final String BOOKING_CREATED = "BOOKING_CREATED";
        public static final String BOOKING_CONFIRMED = "BOOKING_CONFIRMED";
        public static final String BOOKING_CANCELLED = "BOOKING_CANCELLED";
        public static final String PAYMENT_RECEIVED = "PAYMENT_RECEIVED";
        public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
        public static final String REFUND_PROCESSED = "REFUND_PROCESSED";
        public static final String CHECK_IN_REMINDER = "CHECK_IN_REMINDER";
        public static final String CHECK_OUT_REMINDER = "CHECK_OUT_REMINDER";
        public static final String REVIEW_REQUEST = "REVIEW_REQUEST";
    }

    // Cache Keys
    public static final class CacheKeys {
        public static final String BOOKING_PREFIX = "booking:";
        public static final String AVAILABILITY_PREFIX = "availability:";
        public static final String POLICY_PREFIX = "policy:";
        public static final String USER_BOOKINGS_PREFIX = "user_bookings:";
        public static final String PROPERTY_BOOKINGS_PREFIX = "property_bookings:";
        public static final String BOOKING_STATS_PREFIX = "booking_stats:";
        public static final int DEFAULT_TTL_MINUTES = 30;
        public static final int AVAILABILITY_TTL_MINUTES = 15;
        public static final int STATS_TTL_MINUTES = 60;
    }

    // Rate Limiting
    public static final class RateLimits {
        public static final int BOOKING_CREATION_LIMIT = 10; // per hour
        public static final int PAYMENT_ATTEMPT_LIMIT = 5; // per hour
        public static final int CANCELLATION_LIMIT = 3; // per day
        public static final int AVAILABILITY_CHECK_LIMIT = 100; // per minute
        public static final int GUEST_MODIFICATION_LIMIT = 20; // per hour
    }

    // Feature Flags
    public static final class FeatureFlags {
        public static final String INSTANT_BOOKING = "instant_booking_enabled";
        public static final String FLEXIBLE_CANCELLATION = "flexible_cancellation_enabled";
        public static final String SPLIT_PAYMENTS = "split_payments_enabled";
        public static final String GUEST_MESSAGING = "guest_messaging_enabled";
        public static final String AUTOMATIC_CHECK_IN = "automatic_check_in_enabled";
        public static final String DYNAMIC_PRICING = "dynamic_pricing_enabled";
        public static final String MULTI_CURRENCY = "multi_currency_enabled";
    }

    // API Endpoints
    public static final class Endpoints {
        public static final String BASE_PATH = "/api/v1/bookings";
        public static final String AVAILABILITY_PATH = "/api/v1/availability";
        public static final String PAYMENTS_PATH = "/api/v1/booking-payments";
        public static final String POLICIES_PATH = "/api/v1/cancellation-policies";
        public static final String GUESTS_PATH = "/api/v1/booking-guests";
    }

    // HTTP Status Codes
    public static final class HttpStatus {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int CONFLICT = 409;
        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    // Logging Categories
    public static final class LogCategories {
        public static final String BOOKING_OPERATIONS = "BOOKING_OPS";
        public static final String PAYMENT_OPERATIONS = "PAYMENT_OPS";
        public static final String AVAILABILITY_OPERATIONS = "AVAILABILITY_OPS";
        public static final String GUEST_OPERATIONS = "GUEST_OPS";
        public static final String POLICY_OPERATIONS = "POLICY_OPS";
        public static final String VALIDATION_ERRORS = "VALIDATION_ERRORS";
        public static final String BUSINESS_LOGIC_ERRORS = "BUSINESS_LOGIC_ERRORS";
        public static final String EXTERNAL_API_CALLS = "EXTERNAL_API_CALLS";
    }
}
