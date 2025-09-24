package com.imovel.api.payment.stripe.config;

import com.stripe.Stripe;
import com.imovel.api.logger.ApiLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

/**
 * Configuration class for Stripe payment gateway
 * Manages API keys and settings securely
 */
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.public.key}")
    private String publicKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @Value("${stripe.api.version:2023-10-16}")
    private String apiVersion;

    /**
     * Initialize Stripe SDK with configuration
     */
    @PostConstruct
    public void init() {
        validateConfiguration();
        
        // Set Stripe API key
        Stripe.apiKey = secretKey;
        
        ApiLogger.info("StripeConfig", "Stripe SDK initialized successfully with API version: " + apiVersion);
    }

    /**
     * Validate configuration on startup
     */
    private void validateConfiguration() {
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("Stripe secret key is required but not configured");
        }
        
        if (!StringUtils.hasText(publicKey)) {
            throw new IllegalStateException("Stripe public key is required but not configured");
        }
        
        // Validate secret key format (should start with sk_)
        if (!secretKey.startsWith("sk_")) {
            throw new IllegalStateException("Invalid Stripe secret key format. Must start with 'sk_'");
        }
        
        // Validate public key format (should start with pk_)
        if (!publicKey.startsWith("pk_")) {
            throw new IllegalStateException("Invalid Stripe public key format. Must start with 'pk_'");
        }
        
        // Validate webhook secret format if provided (should start with whsec_)
        if (StringUtils.hasText(webhookSecret) && !webhookSecret.startsWith("whsec_")) {
            ApiLogger.error("StripeConfig", "Invalid Stripe webhook secret format. Should start with 'whsec_'");
        }
        
        ApiLogger.info("StripeConfig", "Stripe configuration validation completed successfully");
    }

    // Getters
    public String getSecretKey() {
        return secretKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Get masked secret key for logging (security)
     */
    public String getMaskedSecretKey() {
        if (!StringUtils.hasText(secretKey)) {
            return "NOT_SET";
        }
        return secretKey.substring(0, 7) + "***" + secretKey.substring(secretKey.length() - 4);
    }

    /**
     * Get masked public key for logging (security)
     */
    public String getMaskedPublicKey() {
        if (!StringUtils.hasText(publicKey)) {
            return "NOT_SET";
        }
        return publicKey.substring(0, 7) + "***" + publicKey.substring(publicKey.length() - 4);
    }

    @Override
    public String toString() {
        return "StripeConfig{" +
                "secretKey='" + getMaskedSecretKey() + '\'' +
                ", publicKey='" + getMaskedPublicKey() + '\'' +
                ", webhookSecret='" + (StringUtils.hasText(webhookSecret) ? "SET" : "NOT_SET") + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                '}';
    }
}