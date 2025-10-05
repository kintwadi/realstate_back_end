package com.imovel.api.payment.monitoring;

import com.imovel.api.logger.ApiLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for handling payment-related alerts and notifications
 * Monitors payment failures and system health issues
 */
@Service
public class PaymentAlertService {

    // ApiLogger is a utility class with static methods, no instantiation needed

    private final JavaMailSender mailSender;
    private final AlertConfig alertConfig;
    private final Map<String, LocalDateTime> lastAlertTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failureCounts = new ConcurrentHashMap<>();

    @Autowired
    public PaymentAlertService(JavaMailSender mailSender, AlertConfig alertConfig) {
        this.mailSender = mailSender;
        this.alertConfig = alertConfig;
    }

    /**
     * Record a payment failure and check if alert should be sent
     */
    public void recordPaymentFailure(String gateway, String reason, BigDecimal amount) {
        if (!alertConfig.isEnabled()) {
            return;
        }

        ApiLogger.warn("PaymentAlertService", "Payment failure recorded: gateway=" + gateway + 
                ", reason=" + reason + ", amount=" + amount.toString());

        String key = "payment_failure_" + gateway;
        failureCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();

        // Check if we should send an alert
        if (shouldSendAlert(key, alertConfig.getThresholds().getFailureCount(), 
                           alertConfig.getCooldown().getFailureAlert())) {
            sendPaymentFailureAlert(gateway, reason, amount);
        }
    }

    /**
     * Send alert for high payment failure rate
     */
    public void sendHighFailureRateAlert(double failureRate, int totalPayments, int failedPayments) {
        if (!alertConfig.isEnabled() || !alertConfig.getEmail().isEnabled()) {
            return;
        }
        
        String key = "high_failure_rate";
        if (!shouldSendAlert(key, 1, alertConfig.getCooldown().getFailureAlert())) {
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertConfig.getEmail().getRecipients().toArray(new String[0]));
            message.setFrom(alertConfig.getEmail().getFrom());
            message.setSubject("🚨 HIGH PAYMENT FAILURE RATE ALERT - " + alertConfig.getEmail().getSubject());
            
            String body = String.format(
                "HIGH PAYMENT FAILURE RATE DETECTED\n\n" +
                "Failure Rate: %.2f%%\n" +
                "Total Payments (last hour): %d\n" +
                "Failed Payments: %d\n" +
                "Time: %s\n\n" +
                "Please investigate immediately.\n\n" +
                "Imovel Payment Monitoring System",
                failureRate, totalPayments, failedPayments,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            message.setText(body);
            mailSender.send(message);
            
            ApiLogger.info("PaymentAlertService", "High failure rate alert sent to " + 
                    String.join(",", alertConfig.getEmail().getRecipients()) + 
                    ", failureRate=" + failureRate);
            
            lastAlertTimes.put(key, LocalDateTime.now());
            
        } catch (Exception e) {
            ApiLogger.error("PaymentAlertService", "Failed to send high failure rate alert", e);
        }
    }

    /**
     * Send alert for system health issues
     */
    public void sendSystemHealthAlert(String component, String issue, String details) {
        if (!alertConfig.isEnabled() || !alertConfig.getEmail().isEnabled()) {
            return;
        }
        
        String key = "health_" + component;
        if (!shouldSendAlert(key, 1, alertConfig.getCooldown().getHealthAlert())) {
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertConfig.getEmail().getRecipients().toArray(new String[0]));
            message.setFrom(alertConfig.getEmail().getFrom());
            message.setSubject("🔴 SYSTEM HEALTH ALERT - " + component + " - " + alertConfig.getEmail().getSubject());
            
            String body = String.format(
                "SYSTEM HEALTH ISSUE DETECTED\n\n" +
                "Component: %s\n" +
                "Issue: %s\n" +
                "Details: %s\n" +
                "Time: %s\n\n" +
                "Please investigate immediately.\n\n" +
                "Imovel Payment Monitoring System",
                component, issue, details,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            message.setText(body);
            mailSender.send(message);
            
            ApiLogger.info("PaymentAlertService", "System health alert sent for component=" + 
                    component + ", issue=" + issue);
            
            lastAlertTimes.put(key, LocalDateTime.now());
            
        } catch (Exception e) {
            ApiLogger.error("PaymentAlertService", "Failed to send system health alert", e);
        }
    }





    /**
     * Check if enough time has passed since last alert to send a new one
     */
    private boolean shouldSendAlert(String key, int threshold, Duration cooldown) {
        AtomicInteger count = failureCounts.get(key);
        if (count == null || count.get() < threshold) {
            return false;
        }

        LocalDateTime lastAlert = lastAlertTimes.get(key);
        if (lastAlert == null) {
            return true;
        }

        return LocalDateTime.now().isAfter(lastAlert.plus(cooldown));
    }

    /**
     * Send payment failure alert
     */
    private void sendPaymentFailureAlert(String gateway, String reason, BigDecimal amount) {
        if (!alertConfig.getEmail().isEnabled()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertConfig.getEmail().getRecipients().toArray(new String[0]));
            message.setFrom(alertConfig.getEmail().getFrom());
            message.setSubject("🚨 PAYMENT FAILURE ALERT - " + alertConfig.getEmail().getSubject());

            String body = String.format(
                "PAYMENT FAILURE DETECTED\n\n" +
                "Gateway: %s\n" +
                "Reason: %s\n" +
                "Amount: %s\n" +
                "Time: %s\n\n" +
                "Please investigate payment processing issues.\n\n" +
                "Imovel Payment Monitoring System",
                gateway, reason, amount.toString(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            message.setText(body);
            mailSender.send(message);

            ApiLogger.info("PaymentAlertService", "Payment failure alert sent for gateway=" + 
                    gateway + ", reason=" + reason);

        } catch (Exception e) {
            ApiLogger.error("PaymentAlertService", "Failed to send payment failure alert", e);
        }
    }

    /**
     * Send webhook failure alert
     */
    public void sendWebhookFailureAlert(String gateway, String eventType) {
        if (!alertConfig.isEnabled() || !alertConfig.getEmail().isEnabled()) {
            return;
        }

        String key = "webhook_failure_" + gateway + "_" + eventType;
        if (!shouldSendAlert(key, 1, alertConfig.getCooldown().getWebhookAlert())) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertConfig.getEmail().getRecipients().toArray(new String[0]));
            message.setFrom(alertConfig.getEmail().getFrom());
            message.setSubject("⚠️ WEBHOOK FAILURE ALERT - " + alertConfig.getEmail().getSubject());

            String body = String.format(
                "WEBHOOK FAILURE DETECTED\n\n" +
                "Gateway: %s\n" +
                "Event Type: %s\n" +
                "Time: %s\n\n" +
                "Please check webhook processing.\n\n" +
                "Imovel Payment Monitoring System",
                gateway, eventType,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            message.setText(body);
            mailSender.send(message);

            ApiLogger.info("PaymentAlertService", "Webhook failure alert sent for gateway=" + 
                    gateway + ", eventType=" + eventType);

            lastAlertTimes.put(key, LocalDateTime.now());

        } catch (Exception e) {
            ApiLogger.error("PaymentAlertService", "Failed to send webhook failure alert", e);
        }
    }

    /**
     * Send rate limit alert
     */
    public void sendRateLimitAlert(String endpoint, String userId) {
        if (!alertConfig.isEnabled() || !alertConfig.getEmail().isEnabled()) {
            return;
        }

        String key = "rate_limit_" + endpoint;
        if (!shouldSendAlert(key, 1, alertConfig.getCooldown().getRateLimitAlert())) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertConfig.getEmail().getRecipients().toArray(new String[0]));
            message.setFrom(alertConfig.getEmail().getFrom());
            message.setSubject("🚦 RATE LIMIT ALERT - " + alertConfig.getEmail().getSubject());

            String body = String.format(
                "RATE LIMIT HIT\n\n" +
                "Endpoint: %s\n" +
                "User: %s\n" +
                "Time: %s\n\n" +
                "Consider reviewing rate limit settings.\n\n" +
                "Imovel Payment Monitoring System",
                endpoint, userId != null ? "authenticated" : "anonymous",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            message.setText(body);
            mailSender.send(message);

            ApiLogger.info("PaymentAlertService", "Rate limit alert sent for endpoint=" + 
                    endpoint + ", userId=" + (userId != null ? userId : "anonymous"));

            lastAlertTimes.put(key, LocalDateTime.now());

        } catch (Exception e) {
            ApiLogger.error("PaymentAlertService", "Failed to send rate limit alert", e);
        }
    }

    /**
     * Send failure alert email
     */
    private void sendFailureAlert(int failures, String gateway, String reason, BigDecimal amount) {
        if (!alertConfig.isEnabled() || !alertConfig.getEmail().isEnabled()) {
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertConfig.getEmail().getRecipients().toArray(new String[0]));
            message.setSubject("🚨 PAYMENT FAILURE THRESHOLD EXCEEDED - " + alertConfig.getEmail().getSubject());
            
            String body = String.format(
                "PAYMENT FAILURE THRESHOLD EXCEEDED\n\n" +
                "Recent Failures: %d (threshold: %d)\n" +
                "Time Window: %d minutes\n" +
                "Latest Gateway: %s\n" +
                "Latest Reason: %s\n" +
                "Latest Amount: %s\n" +
                "Time: %s\n\n" +
                "Please investigate payment processing issues immediately.\n\n" +
                "Imovel Payment Monitoring System",
                failures, alertConfig.getThresholds().getFailureCount(),
                alertConfig.getThresholds().getTimeWindow().toMinutes(),
                gateway, reason, amount,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            message.setText(body);
            mailSender.send(message);
            
            ApiLogger.info("PaymentAlertService", "Payment failure alert sent: failures=" + 
                    failures + ", gateway=" + gateway);
            
        } catch (Exception e) {
            ApiLogger.error("PaymentAlertService", "Failed to send payment failure alert", e);
        }
    }
}
