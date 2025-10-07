package com.imovel.api.payment.audit;

/**
 * Enumeration of audit event types for payment module.
 * Provides consistent event type definitions for audit logging.
 */
public enum AuditEventType {
    
    // Payment Processing Events
    PAYMENT_INITIATED("Payment processing initiated"),
    PAYMENT_PROCESSING("Payment being processed"),
    PAYMENT_COMPLETED("Payment completed successfully"),
    PAYMENT_FAILED("Payment processing failed"),
    PAYMENT_CANCELLED("Payment cancelled"),
    PAYMENT_STATUS_CHANGE("Payment status changed"),
    
    // Refund Events
    REFUND_INITIATED("Refund processing initiated"),
    REFUND_PROCESSING("Refund being processed"),
    REFUND_COMPLETED("Refund completed successfully"),
    REFUND_FAILED("Refund processing failed"),
    
    // Verification Events
    PAYMENT_VERIFICATION("Payment status verification"),
    PAYMENT_VERIFICATION_MISMATCH("Payment status mismatch detected"),
    
    // Webhook Events
    WEBHOOK_RECEIVED("Webhook event received"),
    WEBHOOK_PROCESSED("Webhook event processed"),
    WEBHOOK_FAILED("Webhook processing failed"),
    WEBHOOK_SIGNATURE_INVALID("Invalid webhook signature"),
    
    // Security Events
    SECURITY_EVENT("Security event detected"),
    RATE_LIMIT_EXCEEDED("Rate limit exceeded"),
    INVALID_API_KEY("Invalid API key used"),
    UNAUTHORIZED_ACCESS("Unauthorized access attempt"),
    SUSPICIOUS_ACTIVITY("Suspicious activity detected"),
    
    // Configuration Events
    CONFIGURATION_LOADED("Configuration loaded"),
    CONFIGURATION_UPDATED("Configuration updated"),
    CONFIGURATION_VALIDATION_FAILED("Configuration validation failed"),
    
    // System Events
    SYSTEM_STARTUP("System startup"),
    SYSTEM_SHUTDOWN("System shutdown"),
    HEALTH_CHECK("Health check performed"),
    
    // Data Events
    DATA_EXPORT("Data exported"),
    DATA_IMPORT("Data imported"),
    DATA_DELETION("Data deleted"),
    DATA_ANONYMIZATION("Data anonymized");
    
    private final String description;
    
    AuditEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return this.name();
    }
}
