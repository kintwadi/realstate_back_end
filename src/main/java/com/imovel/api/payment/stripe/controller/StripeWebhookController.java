package com.imovel.api.payment.stripe.controller;

import com.imovel.api.logger.ApiLogger;
import com.imovel.api.payment.audit.PaymentAuditLogger;
import com.imovel.api.payment.monitoring.PaymentMonitoringService;
import com.imovel.api.payment.service.PaymentService;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.instrument.Timer;

@RestController
@RequestMapping("/api/webhooks/stripe")
@CrossOrigin(origins = "*")
public class StripeWebhookController {
    
    private final PaymentService paymentService;
    private final PaymentMonitoringService monitoringService;
    // ApiLogger is a utility class with static methods, no instantiation needed
    
    @Autowired
    public StripeWebhookController(PaymentService paymentService, PaymentMonitoringService monitoringService) {
        this.paymentService = paymentService;
        this.monitoringService = monitoringService;
    }
    
    /**
     * Handle Stripe webhook events
     */
    @PostMapping("/events")
    @RateLimiter(name = "webhook", fallbackMethod = "webhookRateLimitFallback")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        Timer.Sample timerSample = monitoringService.startTimer();
        
        ApiLogger.info("Received Stripe webhook");
        
        // Log webhook received
        PaymentAuditLogger.logWebhookReceived("stripe", payload.length());
        
        try {
            ApplicationResponse<String> response = paymentService.handleWebhook("stripe", payload, signature);
            
            if (response.isSuccess()) {
                PaymentAuditLogger.logWebhookProcessed("stripe", "success");
                
                // Record monitoring metrics
                monitoringService.stopWebhookTimer(timerSample, "stripe", "success");
                monitoringService.recordWebhookEvent("stripe", "success", true);
                
                return ResponseEntity.ok("Webhook processed successfully");
            } else {
                PaymentAuditLogger.logWebhookProcessed("stripe", "failed: " + response.getError().getMessage());
                ApiLogger.error("Webhook processing failed: " + response.getError().getMessage());
                
                // Record monitoring metrics
                monitoringService.stopWebhookTimer(timerSample, "stripe", "failed");
                monitoringService.recordWebhookEvent("stripe", "failed", false);
                
                return ResponseEntity.badRequest().body("Webhook processing failed");
            }
            
        } catch (Exception e) {
            PaymentAuditLogger.logWebhookProcessed("stripe", "error: " + e.getMessage());
            ApiLogger.error("Error processing Stripe webhook", e);
            
            // Record monitoring metrics
            monitoringService.stopWebhookTimer(timerSample, "stripe", "error");
            monitoringService.recordWebhookEvent("stripe", "error", false);
            
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }
    
    public ResponseEntity<String> webhookRateLimitFallback(
            String payload, String signature, Exception ex) {
        ApiLogger.warn("Webhook rate limit exceeded");
        
        // Record rate limit hit
        monitoringService.recordRateLimitHit("webhook", "stripe");
        
        return ResponseEntity.status(429).body("Webhook rate limit exceeded. Please try again later.");
    }
}