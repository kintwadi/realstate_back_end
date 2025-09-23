package com.imovel.api.payment.stripe.controller;

import com.imovel.api.logger.ApiLogger;
import com.imovel.api.payment.service.PaymentService;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/stripe")
@CrossOrigin(origins = "*")
public class StripeWebhookController {
    
    private final PaymentService paymentService;

    @Autowired
    public StripeWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    /**
     * Handle Stripe webhook events
     */
    @PostMapping("/events")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        ApiLogger.info("Received Stripe webhook");
        
        try {
            ApplicationResponse<String> response = paymentService.handleWebhook("stripe", payload, signature);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok("Webhook processed successfully");
            } else {
                ApiLogger.error("Webhook processing failed: " + response.getError().getMessage());
                return ResponseEntity.badRequest().body("Webhook processing failed");
            }
            
        } catch (Exception e) {
            ApiLogger.error("Error processing Stripe webhook", e);
            return ResponseEntity.internalServerError().body("Internal server error");
        }
    }
}