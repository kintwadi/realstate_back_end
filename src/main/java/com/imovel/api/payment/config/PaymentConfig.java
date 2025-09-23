package com.imovel.api.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {
    
    private Stripe stripe = new Stripe();
    private Paypal paypal = new Paypal();
    private General general = new General();
    
    // Getters and setters
    public Stripe getStripe() {
        return stripe;
    }
    
    public void setStripe(Stripe stripe) {
        this.stripe = stripe;
    }
    
    public Paypal getPaypal() {
        return paypal;
    }
    
    public void setPaypal(Paypal paypal) {
        this.paypal = paypal;
    }
    
    public General getGeneral() {
        return general;
    }
    
    public void setGeneral(General general) {
        this.general = general;
    }
    
    // Stripe configuration
    public static class Stripe {
        private String publicKey;
        private String secretKey;
        private String webhookSecret;
        private String apiVersion = "2025-10-16";
        private boolean enabled = true;
        private int connectTimeoutMs = 30000;
        private int readTimeoutMs = 80000;
        
        // Getters and setters
        public String getPublicKey() {
            return publicKey;
        }
        
        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }
        
        public String getSecretKey() {
            return secretKey;
        }
        
        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
        
        public String getWebhookSecret() {
            return webhookSecret;
        }
        
        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }
        
        public String getApiVersion() {
            return apiVersion;
        }
        
        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }
        
        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }
        
        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }
        
        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }
    
    // PayPal configuration (for future implementation)
    public static class Paypal {
        private String clientId;
        private String clientSecret;
        private String webhookId;
        private String environment = "sandbox"; // sandbox or live
        private boolean enabled = false;
        
        // Getters and setters
        public String getClientId() {
            return clientId;
        }
        
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
        
        public String getClientSecret() {
            return clientSecret;
        }
        
        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
        
        public String getWebhookId() {
            return webhookId;
        }
        
        public void setWebhookId(String webhookId) {
            this.webhookId = webhookId;
        }
        
        public String getEnvironment() {
            return environment;
        }
        
        public void setEnvironment(String environment) {
            this.environment = environment;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    // General payment configuration
    public static class General {
        private String defaultCurrency = "USD";
        private BigDecimal minimumAmount = new BigDecimal("0.50");
        private BigDecimal maximumAmount = new BigDecimal("999999.99");
        private int paymentTimeoutMinutes = 30;
        private boolean enableRefunds = true;
        private boolean enablePartialRefunds = true;
        private int maxRefundDays = 90;
        private Map<String, String> supportedCurrencies;
        
        // Getters and setters
        public String getDefaultCurrency() {
            return defaultCurrency;
        }
        
        public void setDefaultCurrency(String defaultCurrency) {
            this.defaultCurrency = defaultCurrency;
        }
        
        public BigDecimal getMinimumAmount() {
            return minimumAmount;
        }
        
        public void setMinimumAmount(BigDecimal minimumAmount) {
            this.minimumAmount = minimumAmount;
        }
        
        public BigDecimal getMaximumAmount() {
            return maximumAmount;
        }
        
        public void setMaximumAmount(BigDecimal maximumAmount) {
            this.maximumAmount = maximumAmount;
        }
        
        public int getPaymentTimeoutMinutes() {
            return paymentTimeoutMinutes;
        }
        
        public void setPaymentTimeoutMinutes(int paymentTimeoutMinutes) {
            this.paymentTimeoutMinutes = paymentTimeoutMinutes;
        }
        
        public boolean isEnableRefunds() {
            return enableRefunds;
        }
        
        public void setEnableRefunds(boolean enableRefunds) {
            this.enableRefunds = enableRefunds;
        }
        
        public boolean isEnablePartialRefunds() {
            return enablePartialRefunds;
        }
        
        public void setEnablePartialRefunds(boolean enablePartialRefunds) {
            this.enablePartialRefunds = enablePartialRefunds;
        }
        
        public int getMaxRefundDays() {
            return maxRefundDays;
        }
        
        public void setMaxRefundDays(int maxRefundDays) {
            this.maxRefundDays = maxRefundDays;
        }
        
        public Map<String, String> getSupportedCurrencies() {
            return supportedCurrencies;
        }
        
        public void setSupportedCurrencies(Map<String, String> supportedCurrencies) {
            this.supportedCurrencies = supportedCurrencies;
        }
    }
}