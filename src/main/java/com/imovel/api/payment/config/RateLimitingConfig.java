package com.imovel.api.payment.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for rate limiting payment endpoints
 * Protects against abuse and ensures system stability
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Rate limiter for payment processing endpoints
     * Allows 10 requests per minute per user
     */
    @Bean
    public RateLimiter paymentProcessingRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10) // 10 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(5)) // wait up to 5 seconds for permission
                .build();
        
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("paymentProcessing");
    }

    /**
     * Rate limiter for payment verification endpoints
     * Allows 30 requests per minute per user (more lenient for read operations)
     */
    @Bean
    public RateLimiter paymentVerificationRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(30) // 30 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(3)) // wait up to 3 seconds for permission
                .build();
        
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("paymentVerification");
    }

    /**
     * Rate limiter for webhook endpoints
     * Allows 100 requests per minute (webhooks from Stripe)
     */
    @Bean
    public RateLimiter webhookRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100) // 100 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(1)) // minimal wait for webhooks
                .build();
        
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("webhook");
    }

    /**
     * Rate limiter for refund operations
     * Allows 5 requests per minute per user (more restrictive for financial operations)
     */
    @Bean
    public RateLimiter refundRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5) // 5 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                .timeoutDuration(Duration.ofSeconds(10)) // longer wait for refunds
                .build();
        
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("refund");
    }
}
