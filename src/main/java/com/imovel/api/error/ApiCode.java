package com.imovel.api.error;

import org.springframework.http.HttpStatus;

public enum ApiCode {
    // General/System Errors (1000-1999)
    SYSTEM_ERROR(1000, "Internal system error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_CONNECTION_ERROR(1001, "Database connection error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(1002, "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_REQUEST(1003, "Invalid request", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(1004, "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),

    // Authentication Module Errors (2000-2099)
    AUTHENTICATION_FAILED(2000, "Authentication failed", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(2001, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(2002, "Account is locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(2003, "Account is disabled", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(2004, "Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(2005, "Invalid token", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(2006, "Access denied", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN(2007, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND(2008, "Refresh token not found", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_NOT_EXPIRED(2009, "Refresh token not expired", HttpStatus.CONFLICT),
    REFRESH_TOKEN_NOT_LIMITE_EXCEEDED(2010, "Token limit exceeded. Oldest tokens.p12 have been revoked.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(2011, "Refresh token  expired", HttpStatus.CONFLICT),
    REFRESH_TOKEN_FAILED(2012, "Refresh token  failed", HttpStatus.CONFLICT),
    // User Module Errors (2100-2199)
    USER_NOT_FOUND(2100, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2101, "User already exists", HttpStatus.CONFLICT),
    INVALID_USER_DATA(2102, "Invalid user data", HttpStatus.BAD_REQUEST),
    PASSWORD_POLICY_VIOLATION(2103, "Password does not meet policy requirements", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_FAILED(2104,"Password reset valied",HttpStatus.CONFLICT),
    USER_PROFILE_INCOMPLETE(2105, "User profile is incomplete", HttpStatus.BAD_REQUEST),
    INAVALID_PASSWORD(2106, "Password does not match", HttpStatus.CONFLICT),

    // Role Module Errors (2200-2299)
    ROLE_NOT_FOUND(2200, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS(2201, "Role already exists", HttpStatus.CONFLICT),
    INVALID_ROLE_DATA(2202, "Invalid role data", HttpStatus.BAD_REQUEST),
    ROLE_IN_USE(2203, "Role is assigned to users and cannot be deleted", HttpStatus.CONFLICT),

    // Permission/Authorization Module Errors (2300-2399)
    PERMISSION_DENIED(2300, "Permission denied", HttpStatus.FORBIDDEN),
    PERMISSION_NOT_FOUND(2301, "Permission not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_PRIVILEGES(2302, "Insufficient privileges", HttpStatus.FORBIDDEN),
    PERMISSION_OR_ROLE_NOT_FOUND(2303, "Permission or Role not found", HttpStatus.NOT_FOUND),

    // Property Module Errors (3000-3099)
    PROPERTY_NOT_FOUND(3000, "Property not found", HttpStatus.NOT_FOUND),
    PROPERTY_ALREADY_EXISTS(3001, "Property already exists", HttpStatus.CONFLICT),
    INVALID_PROPERTY_DATA(3002, "Invalid property data", HttpStatus.BAD_REQUEST),
    PROPERTY_IMAGE_UPLOAD_FAILED(3003, "Property image upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PROPERTY_NOT_AVAILABLE(3004, "Property is not available", HttpStatus.CONFLICT),
    INVALID_PROPERTY_STATUS(3005, "Invalid property status", HttpStatus.BAD_REQUEST),
    PROPERTY_OPERATION_NOT_ALLOWED(3006, "Operation not allowed on this property", HttpStatus.FORBIDDEN),

    RESOURCE_NOT_FOUND(3007, "Resource not found", HttpStatus.NOT_FOUND),

    // Booking Module Errors (3100-3199)
    BOOKING_NOT_FOUND(3100, "Booking not found", HttpStatus.NOT_FOUND),
    INVALID_BOOKING_DATA(3101, "Invalid booking data", HttpStatus.BAD_REQUEST),
    BOOKING_CONFLICT(3102, "Booking dates conflict with existing booking", HttpStatus.CONFLICT),
    BOOKING_CANCELLATION_NOT_ALLOWED(3103, "Booking cancellation not allowed", HttpStatus.FORBIDDEN),
    BOOKING_ALREADY_CONFIRMED(3104, "Booking is already confirmed", HttpStatus.CONFLICT),
    BOOKING_PAYMENT_REQUIRED(3105, "Payment required to confirm booking", HttpStatus.PAYMENT_REQUIRED),
    INVALID_BOOKING_STATUS(3106, "Invalid booking status", HttpStatus.BAD_REQUEST),

    // Subscription Module Errors (3200-3299)
    SUBSCRIPTION_NOT_FOUND(3200, "Subscription not found", HttpStatus.NOT_FOUND),
    SUBSCRIPTION_ALREADY_EXISTS(3201, "Subscription already exists", HttpStatus.CONFLICT),
    INVALID_SUBSCRIPTION_DATA(3202, "Invalid subscription data", HttpStatus.BAD_REQUEST),
    SUBSCRIPTION_EXPIRED(3203, "Subscription has expired", HttpStatus.FORBIDDEN),
    SUBSCRIPTION_LIMIT_REACHED(3204, "Subscription limit reached", HttpStatus.FORBIDDEN),
    SUBSCRIPTION_PAYMENT_FAILED(3205, "Subscription payment failed", HttpStatus.PAYMENT_REQUIRED),
    INVALID_SUBSCRIPTION_PLAN(3206, "Invalid subscription plan", HttpStatus.BAD_REQUEST),

    // Validation Errors (4000-4099)
    VALIDATION_ERROR(4000, "Validation error", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING(4001, "Required field is missing", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(4002, "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT(4003, "Invalid phone number format", HttpStatus.BAD_REQUEST),
    INVALID_DATE_FORMAT(4004, "Invalid date format", HttpStatus.BAD_REQUEST),
    INVALID_NUMERIC_VALUE(4005, "Invalid numeric value", HttpStatus.BAD_REQUEST),
    VALUE_OUT_OF_RANGE(4006, "Value out of allowed range", HttpStatus.BAD_REQUEST),

    // External Service Errors (5000-5099)
    PAYMENT_GATEWAY_ERROR(5000, "Payment gateway error", HttpStatus.BAD_GATEWAY),
    MAP_SERVICE_ERROR(5001, "Map service error", HttpStatus.BAD_GATEWAY),
    EMAIL_SERVICE_ERROR(5002, "Email service error", HttpStatus.INTERNAL_SERVER_ERROR),
    SMS_SERVICE_ERROR(5003, "SMS service error", HttpStatus.INTERNAL_SERVER_ERROR),
    // Validation
    INVALID_PAYLOAD(600, "invalid payload", HttpStatus.BAD_REQUEST),
    MISSING_FIELDS(601,"Missing required fields",HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(602,"Invalid email format",HttpStatus.CONFLICT),
    INVALID_EMAIL_ALREADY_EXIST(603,"Email already exist",HttpStatus.CONFLICT),

    // Permission/Authorization Module Errors section (2300-2399)
    ROLE_CREATION_FAILED(2303, "Failed to create role", HttpStatus.INTERNAL_SERVER_ERROR),
    PERMISSION_CREATION_FAILED(2304, "Failed to create permission", HttpStatus.INTERNAL_SERVER_ERROR),
    PERMISSION_ASSIGNMENT_FAILED(2305, "Failed to assign permission to role", HttpStatus.INTERNAL_SERVER_ERROR),
    PERMISSION_REMOVAL_FAILED(2306, "Failed to remove permission from role", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_ASSIGNMENT_FAILED(2307, "Failed to assign role to user", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_REMOVAL_FAILED(2308, "Failed to remove role from user", HttpStatus.INTERNAL_SERVER_ERROR),
    PERMISSION_RETRIEVAL_FAILED(2309, "Failed to retrieve permissions", HttpStatus.INTERNAL_SERVER_ERROR),
    INITIALIZATION_FAILED(2310, "Failed to initialize default roles and permissions", HttpStatus.INTERNAL_SERVER_ERROR),
    PARSE_ERROR(2312, "Failed to parse data", HttpStatus.INTERNAL_SERVER_ERROR),
    // Add this to your ApiCode enum in the Authentication Module Errors section
    PAYMENT_ACCESS_DENIED(2007, "Payment access denied", HttpStatus.FORBIDDEN),
    USER_ID_MISMATCH(2008, "User ID mismatch", HttpStatus.FORBIDDEN);


    private final Integer code;
    private final String message;
    private final HttpStatus httpStatus;

    ApiCode(Integer code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
