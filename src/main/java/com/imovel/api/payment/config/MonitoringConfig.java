package com.imovel.api.payment.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for payment monitoring and metrics
 */
@Configuration
public class MonitoringConfig {

    /**
     * Counter for successful payments
     */
    @Bean
    public Counter paymentSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("payment.success")
                .description("Number of successful payments")
                .tag("type", "success")
                .register(meterRegistry);
    }

    /**
     * Counter for failed payments
     */
    @Bean
    public Counter paymentFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("payment.failure")
                .description("Number of failed payments")
                .tag("type", "failure")
                .register(meterRegistry);
    }

    /**
     * Counter for payment refunds
     */
    @Bean
    public Counter paymentRefundCounter(MeterRegistry meterRegistry) {
        return Counter.builder("payment.refund")
                .description("Number of payment refunds")
                .tag("type", "refund")
                .register(meterRegistry);
    }

    /**
     * Counter for webhook events
     */
    @Bean
    public Counter webhookEventCounter(MeterRegistry meterRegistry) {
        return Counter.builder("webhook.events")
                .description("Number of webhook events received")
                .tag("type", "webhook")
                .register(meterRegistry);
    }

    /**
     * Timer for payment processing duration
     */
    @Bean
    public Timer paymentProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("payment.processing.duration")
                .description("Time taken to process payments")
                .register(meterRegistry);
    }

    /**
     * Timer for refund processing duration
     */
    @Bean
    public Timer refundProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("refund.processing.duration")
                .description("Time taken to process refunds")
                .register(meterRegistry);
    }

    /**
     * Timer for webhook processing duration
     */
    @Bean
    public Timer webhookProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("webhook.processing.duration")
                .description("Time taken to process webhooks")
                .register(meterRegistry);
    }

    /**
     * Counter for rate limit hits
     */
    @Bean
    public Counter rateLimitCounter(MeterRegistry meterRegistry) {
        return Counter.builder("payment.rate_limit")
                .description("Number of rate limit hits")
                .tag("type", "rate_limit")
                .register(meterRegistry);
    }

    /**
     * Counter for security events
     */
    @Bean
    public Counter securityEventCounter(MeterRegistry meterRegistry) {
        return Counter.builder("payment.security")
                .description("Number of security events")
                .tag("type", "security")
                .register(meterRegistry);
    }
}