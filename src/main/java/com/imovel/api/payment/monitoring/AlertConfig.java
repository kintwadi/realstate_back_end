package com.imovel.api.payment.monitoring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for payment monitoring alerts
 */
@Configuration
@ConfigurationProperties(prefix = "payment.monitoring.alerts")
public class AlertConfig {

    private boolean enabled = true;
    private Email email = new Email();
    private Thresholds thresholds = new Thresholds();
    private Cooldown cooldown = new Cooldown();

    public static class Email {
        private boolean enabled = true;
        private List<String> recipients = List.of("admin@example.com");
        private String from = "noreply@example.com";
        private String subject = "Payment System Alert";

        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
    }

    public static class Thresholds {
        private double failureRate = 0.1; // 10%
        private int failureCount = 5;
        private Duration timeWindow = Duration.ofMinutes(5);

        // Getters and setters
        public double getFailureRate() { return failureRate; }
        public void setFailureRate(double failureRate) { this.failureRate = failureRate; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public Duration getTimeWindow() { return timeWindow; }
        public void setTimeWindow(Duration timeWindow) { this.timeWindow = timeWindow; }
    }

    public static class Cooldown {
        private Duration failureAlert = Duration.ofMinutes(15);
        private Duration healthAlert = Duration.ofMinutes(30);
        private Duration webhookAlert = Duration.ofMinutes(10);
        private Duration rateLimitAlert = Duration.ofMinutes(5);

        // Getters and setters
        public Duration getFailureAlert() { return failureAlert; }
        public void setFailureAlert(Duration failureAlert) { this.failureAlert = failureAlert; }
        
        public Duration getHealthAlert() { return healthAlert; }
        public void setHealthAlert(Duration healthAlert) { this.healthAlert = healthAlert; }
        
        public Duration getWebhookAlert() { return webhookAlert; }
        public void setWebhookAlert(Duration webhookAlert) { this.webhookAlert = webhookAlert; }
        
        public Duration getRateLimitAlert() { return rateLimitAlert; }
        public void setRateLimitAlert(Duration rateLimitAlert) { this.rateLimitAlert = rateLimitAlert; }
    }

    // Main getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }
    
    public Thresholds getThresholds() { return thresholds; }
    public void setThresholds(Thresholds thresholds) { this.thresholds = thresholds; }
    
    public Cooldown getCooldown() { return cooldown; }
    public void setCooldown(Cooldown cooldown) { this.cooldown = cooldown; }
}