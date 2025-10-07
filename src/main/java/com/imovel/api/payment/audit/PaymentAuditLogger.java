package com.imovel.api.payment.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Specialized audit logger for payment events and transactions.
 * Provides structured logging with consistent format for audit trails.
 * Uses ApiLogger for consistent logging across the application.
 */
public final class PaymentAuditLogger {
    
    private static final String AUDIT_LOGGER_TYPE = "PAYMENT_AUDIT";
    private static final Logger auditLogger = LoggerFactory.getLogger(AUDIT_LOGGER_TYPE);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // Note: Using dedicated SLF4J logger to prevent circular logging
    // The PAYMENT_AUDIT logger is configured directly in log4j2.xml
    
    private PaymentAuditLogger() {}
    
    /**
     * Log payment processing events
     */
    public static void logPaymentProcessing(Long userId, String paymentId, BigDecimal amount, 
                                          String currency, String gateway, String status) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_PROCESSING", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("amount", amount);
        auditData.put("currency", currency);
        auditData.put("gateway", gateway);
        auditData.put("status", status);
        
        logAuditEvent(auditData);
    }
    
    /**
     * Log payment status changes
     */
    public static void logPaymentStatusChange(Long userId, String paymentId, String oldStatus, 
                                            String newStatus, String reason) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_STATUS_CHANGE", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("oldStatus", oldStatus);
        auditData.put("newStatus", newStatus);
        auditData.put("reason", reason);
        
        logAuditEvent(auditData);
    }
    
    /**
     * Log refund processing events
     */
    public static void logRefundProcessing(Long userId, String paymentId, String refundId, 
                                         BigDecimal refundAmount, String reason) {
        Map<String, Object> auditData = createBaseAuditData("REFUND_PROCESSING", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("refundId", refundId);
        auditData.put("refundAmount", refundAmount);
        auditData.put("reason", reason);
        
        logAuditEvent(auditData);
    }
    
    /**
     * Log webhook events
     */
    public static void logWebhookEvent(String gateway, String eventType, String eventId, 
                                     String paymentId, String status) {
        Map<String, Object> auditData = createBaseAuditData("WEBHOOK_EVENT", null);
        auditData.put("gateway", gateway);
        auditData.put("eventType", eventType);
        auditData.put("eventId", eventId);
        auditData.put("paymentId", paymentId);
        auditData.put("status", status);
        
        logAuditEvent(auditData);
    }
    
    /**
     * Log payment verification events
     */
    public static void logPaymentVerification(Long userId, String paymentId, String gatewayStatus, 
                                            String localStatus, boolean statusMatch) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_VERIFICATION", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("gatewayStatus", gatewayStatus);
        auditData.put("localStatus", localStatus);
        auditData.put("statusMatch", statusMatch);
        
        logAuditEvent(auditData);
    }
    
    /**
     * Log security events
     */
    public static void logSecurityEvent(String eventType, String description, String ipAddress, 
                                      String userAgent, Long userId) {
        Map<String, Object> auditData = createBaseAuditData("SECURITY_EVENT", userId);
        auditData.put("securityEventType", eventType);
        auditData.put("description", description);
        auditData.put("ipAddress", ipAddress);
        auditData.put("userAgent", userAgent);
        
        logAuditEvent(auditData);
    }
    
    /**
     * Log rate limiting events
     */
    public static void logRateLimitEvent(String endpoint, String rateLimiterName, Long userId, 
                                       String ipAddress) {
        Map<String, Object> auditData = createBaseAuditData("RATE_LIMIT_EXCEEDED", userId);
        auditData.put("endpoint", endpoint);
        auditData.put("rateLimiterName", rateLimiterName);
        auditData.put("ipAddress", ipAddress);
        
        logAuditEvent(auditData);
    }
    
    /**
     * Log configuration events
     */
    public static void logConfigurationEvent(String configType, String action, String details) {
        Map<String, Object> auditData = createBaseAuditData("CONFIGURATION_EVENT", null);
        auditData.put("configType", configType);
        auditData.put("action", action);
        auditData.put("details", details);
        
        logAuditEvent(auditData);
    }

    /**
     * Log payment initiation
     */
    public static void logPaymentInitiated(Long userId, BigDecimal amount, String currency, String gateway) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_INITIATED", userId);
        auditData.put("amount", amount);
        auditData.put("currency", currency);
        auditData.put("gateway", gateway);
        
        logAuditEvent(auditData);
    }

    /**
     * Log payment validation failure
     */
    public static void logPaymentValidationFailed(Long userId, String reason) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_VALIDATION_FAILED", userId);
        auditData.put("reason", reason);
        
        logAuditEvent(auditData);
    }

    /**
     * Log payment creation
     */
    public static void logPaymentCreated(Long paymentId, Long userId, BigDecimal amount, String currency, String gateway) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_CREATED", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("amount", amount);
        auditData.put("currency", currency);
        auditData.put("gateway", gateway);
        
        logAuditEvent(auditData);
    }

    /**
     * Log payment failure
     */
    public static void logPaymentFailed(Long paymentId, Long userId, String reason) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_FAILED", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("reason", reason);
        
        logAuditEvent(auditData);
    }

    /**
     * Log payment completion
     */
    public static void logPaymentCompleted(Long paymentId, Long userId, BigDecimal amount, String currency, String gatewayPaymentId) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_COMPLETED", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("amount", amount);
        auditData.put("currency", currency);
        auditData.put("gatewayPaymentId", gatewayPaymentId);
        
        logAuditEvent(auditData);
    }

    /**
     * Log payment error
     */
    public static void logPaymentError(Long userId, String error) {
        Map<String, Object> auditData = createBaseAuditData("PAYMENT_ERROR", userId);
        auditData.put("error", error);
        
        logAuditEvent(auditData);
    }

    /**
     * Log refund initiation
     */
    public static void logRefundInitiated(Long paymentId, Long userId, BigDecimal amount, String reason) {
        Map<String, Object> auditData = createBaseAuditData("REFUND_INITIATED", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("amount", amount);
        auditData.put("reason", reason);
        
        logAuditEvent(auditData);
    }

    /**
     * Log refund failure
     */
    public static void logRefundFailed(Long paymentId, Long userId, String reason) {
        Map<String, Object> auditData = createBaseAuditData("REFUND_FAILED", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("reason", reason);
        
        logAuditEvent(auditData);
    }

    /**
     * Log refund completion
     */
    public static void logRefundCompleted(Long paymentId, Long userId, BigDecimal amount, String refundId) {
        Map<String, Object> auditData = createBaseAuditData("REFUND_COMPLETED", userId);
        auditData.put("paymentId", paymentId);
        auditData.put("amount", amount);
        auditData.put("refundId", refundId);
        
        logAuditEvent(auditData);
    }

    /**
     * Log webhook received
     */
    public static void logWebhookReceived(String gateway, int payloadLength) {
        Map<String, Object> auditData = createBaseAuditData("WEBHOOK_RECEIVED", null);
        auditData.put("gateway", gateway);
        auditData.put("payloadLength", payloadLength);
        
        logAuditEvent(auditData);
    }

    /**
     * Log webhook processed
     */
    public static void logWebhookProcessed(String gateway, String status) {
        Map<String, Object> auditData = createBaseAuditData("WEBHOOK_PROCESSED", null);
        auditData.put("gateway", gateway);
        auditData.put("status", status);
        
        logAuditEvent(auditData);
    }

    /**
     * Log webhook event received
     */
    public static void logWebhookEventReceived(String gateway, String eventType, String eventId) {
        Map<String, Object> auditData = createBaseAuditData("WEBHOOK_EVENT_RECEIVED", null);
        auditData.put("gateway", gateway);
        auditData.put("eventType", eventType);
        auditData.put("eventId", eventId);
        
        logAuditEvent(auditData);
    }
    
    private static Map<String, Object> createBaseAuditData(String eventType, Long userId) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        auditData.put("eventType", eventType);
        auditData.put("userId", userId);
        auditData.put("sessionId", MDC.get("sessionId"));
        auditData.put("requestId", MDC.get("requestId"));
        auditData.put("correlationId", MDC.get("correlationId"));
        
        return auditData;
    }
    
    private static void logAuditEvent(Map<String, Object> auditData) {
        try {
            String jsonLog = objectMapper.writeValueAsString(auditData);
            // Use dedicated audit logger to prevent circular logging
            auditLogger.info(jsonLog);
        } catch (Exception e) {
            // Fallback to simple logging if JSON serialization fails
            auditLogger.error("Failed to serialize audit data: {}", auditData, e);
        }
    }
}
