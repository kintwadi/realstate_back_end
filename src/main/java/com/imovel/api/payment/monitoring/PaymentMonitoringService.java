package com.imovel.api.payment.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Service for collecting payment-related metrics and monitoring data
 */
@Service
public class PaymentMonitoringService {

    private final MeterRegistry meterRegistry;
    private final Timer paymentProcessingTimer;
    private final Timer refundProcessingTimer;
    private final Timer webhookProcessingTimer;
    private final PaymentAlertService alertService;

    @Autowired
    public PaymentMonitoringService(MeterRegistry meterRegistry, PaymentAlertService alertService) {
        this.meterRegistry = meterRegistry;
        this.alertService = alertService;
        this.paymentProcessingTimer = Timer.builder("payment.processing.time")
                .description("Payment processing time")
                .register(meterRegistry);
        this.refundProcessingTimer = Timer.builder("refund.processing.time")
                .description("Refund processing time")
                .register(meterRegistry);
        this.webhookProcessingTimer = Timer.builder("webhook.processing.time")
                .description("Webhook processing time")
                .register(meterRegistry);
    }

    /**
     * Record a successful payment
     */
    public void recordPaymentSuccess(String gateway, BigDecimal amount, String currency) {
        meterRegistry.counter("payment.success",
                "gateway", gateway,
                "currency", currency,
                "amount_range", categorizeAmount(amount)
        ).increment();
    }

    /**
     * Record a failed payment
     */
    public void recordPaymentFailure(String gateway, String reason, BigDecimal amount) {
        meterRegistry.counter("payment.failure",
                "gateway", gateway,
                "reason", reason,
                "amount_range", categorizeAmount(amount)
        ).increment();
        
        // Send alert for payment failure
        alertService.recordPaymentFailure(gateway, reason, amount);
    }

    /**
     * Record a payment refund
     */
    public void recordPaymentRefund(String gateway, BigDecimal amount, String currency) {
        meterRegistry.counter("payment.refund",
                "gateway", gateway,
                "currency", currency,
                "amount_range", categorizeAmount(amount)
        ).increment();
    }

    /**
     * Record webhook event
     */
    public void recordWebhookEvent(String gateway, String eventType, boolean success) {
        meterRegistry.counter("webhook.event",
                "gateway", gateway,
                "event_type", eventType,
                "success", String.valueOf(success)
        ).increment();
        
        if (!success) {
            // Send alert for webhook failure
            alertService.sendWebhookFailureAlert(gateway, eventType);
        }
    }

    /**
     * Record payment processing time
     */
    public void recordPaymentProcessingTime(Duration duration, String gateway, boolean success) {
        paymentProcessingTimer.record(duration.toNanos(), java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * Record refund processing time
     */
    public void recordRefundProcessingTime(Duration duration, String gateway, boolean success) {
        refundProcessingTimer.record(duration.toNanos(), java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * Record webhook processing time
     */
    public void recordWebhookProcessingTime(Duration duration, String gateway, String eventType) {
        webhookProcessingTimer.record(duration.toNanos(), java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * Record rate limit hit
     */
    public void recordRateLimitHit(String endpoint, String userId) {
        meterRegistry.counter("rate.limit.hit",
                "endpoint", endpoint,
                "user_id", userId != null ? "authenticated" : "anonymous"
        ).increment();
        
        // Send alert for rate limit hits
        alertService.sendRateLimitAlert(endpoint, userId);
    }

    /**
     * Record security event
     */
    public void recordSecurityEvent(String eventType, String severity, String source) {
        meterRegistry.counter("security.event",
                "event_type", eventType,
                "severity", severity,
                "source", source
        ).increment();
    }

    /**
     * Categorize amount for metrics
     */
    private String categorizeAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(10)) <= 0) {
            return "0-10";
        } else if (amount.compareTo(BigDecimal.valueOf(100)) <= 0) {
            return "10-100";
        } else if (amount.compareTo(BigDecimal.valueOf(1000)) <= 0) {
            return "100-1000";
        } else if (amount.compareTo(BigDecimal.valueOf(10000)) <= 0) {
            return "1000-10000";
        } else {
            return "10000+";
        }
    }

    /**
     * Create a timer sample for measuring duration
     */
    public Timer.Sample startTimer() {
        return Timer.start();
    }

    /**
     * Stop timer and record payment processing time
     */
    public void stopPaymentTimer(Timer.Sample sample, String gateway, boolean success) {
        sample.stop(paymentProcessingTimer);
    }

    /**
     * Stop timer and record refund processing time
     */
    public void stopRefundTimer(Timer.Sample sample, String gateway, boolean success) {
        sample.stop(refundProcessingTimer);
    }

    /**
     * Stop timer and record webhook processing time
     */
    public void stopWebhookTimer(Timer.Sample sample, String gateway, String eventType) {
        sample.stop(webhookProcessingTimer);
    }
}
