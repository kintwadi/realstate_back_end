package com.imovel.api.payment.controller;

import com.imovel.api.logger.ApiLogger;
import com.imovel.api.payment.audit.PaymentAuditLogger;
import com.imovel.api.payment.dto.PaymentRefundRequest;
import com.imovel.api.payment.dto.PaymentRequest;
import com.imovel.api.payment.dto.PaymentResponse;
import com.imovel.api.payment.monitoring.PaymentMonitoringService;
import com.imovel.api.payment.service.PaymentService;
import com.imovel.api.response.ApplicationResponse;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import com.imovel.api.pagination.Pagination;
import com.imovel.api.pagination.PaginationResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
//@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PaymentMonitoringService monitoringService;
    // ApiLogger is a utility class with static methods, no instantiation needed
    
    @Autowired
    public PaymentController(PaymentService paymentService,PaymentMonitoringService monitoringService) {
        this.paymentService = paymentService;
        this.monitoringService = monitoringService;
    }
    
    /**
     * Process a new payment
     */
    @PostMapping("/process")
    @RateLimiter(name = "paymentProcessing", fallbackMethod = "paymentRateLimitFallback")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> processPayment(
             @RequestBody PaymentRequest paymentRequest) {
        
        ApiLogger.info("Processing payment request for user: " + paymentRequest.getUserId());
        
        ApplicationResponse<PaymentResponse> response = paymentService.processPayment(paymentRequest, paymentRequest.getUserId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }
    
    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long paymentId,
            @RequestParam Long userId) {
        
        ApiLogger.info("Retrieving payment by ID: " + paymentId + " for user: " + userId);
        
        ApplicationResponse<PaymentResponse> response = paymentService.getPaymentById(paymentId, userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }
    
    /**
     * Get user's payment history with pagination
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApplicationResponse<PaginationResult<PaymentResponse>>> getUserPayments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        ApiLogger.info("Retrieving payments for user: " + userId);
        
        Pagination pagination = new Pagination();
        pagination.setPageNumber(page);
        pagination.setPageSize(size);
        
        ApplicationResponse<PaginationResult<PaymentResponse>> response = paymentService.getUserPayments(userId, pagination, sortBy, sortDirection);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }
    
    /**
     * Process a refund
     */
    @PostMapping("/refund")
    @RateLimiter(name = "paymentRefund", fallbackMethod = "refundRateLimitFallback")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> processRefund(@RequestBody PaymentRefundRequest paymentRefund) {


        // Assign variables from the request object
        Long paymentId = paymentRefund.getPaymentId();
        BigDecimal refundAmount = paymentRefund.getRefundAmount();
        String reason = paymentRefund.getReason();
        Long userId = paymentRefund.getUserId();

        ApiLogger.info("Processing refund for payment: " + paymentId + ", amount: " + refundAmount);
        
        ApplicationResponse<PaymentResponse> response = paymentService.processRefund(
            paymentId, refundAmount, reason, userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }
    
    /**
     * Cancel a payment
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam Long userId) {
        
        ApiLogger.info("Cancelling payment: " + paymentId + " for user: " + userId);
        
        ApplicationResponse<PaymentResponse> response = paymentService.cancelPayment(paymentId, userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }
    
    /**
     * Verify payment status with gateway
     */
    @PostMapping("/{paymentId}/verify")
    @RateLimiter(name = "paymentVerification", fallbackMethod = "verificationRateLimitFallback")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> verifyPaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam Long userId) {
        
        ApiLogger.info("Verifying payment status for payment: " + paymentId + " for user: " + userId);
        
        ApplicationResponse<PaymentResponse> response = paymentService.verifyPaymentStatus(paymentId, userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }
    
    /**
     * Get payment statistics for a user within a date range
     */
    @GetMapping("/statistics/{userId}")
    public ResponseEntity<ApplicationResponse<PaymentService.PaymentStatistics>> getPaymentStatistics(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        ApiLogger.info("Retrieving payment statistics for user: " + userId);
        
        ApplicationResponse<PaymentService.PaymentStatistics> response = 
            paymentService.getPaymentStatistics(userId, startDate, endDate);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }

//    @PostMapping("/webhooks/events")
//    public ResponseEntity<String> handleStripeWebhook(
//            @RequestBody String payload,
//            @RequestHeader("Stripe-Signature") String signature) {
//
//        Timer.Sample timerSample = monitoringService.startTimer();
//
//        ApiLogger.info("Received Stripe webhook");
//
//        // Log webhook received
//        PaymentAuditLogger.logWebhookReceived("stripe", payload.length());
//
//        try {
//            ApplicationResponse<String> response = paymentService.handleWebhook("stripe", payload, signature);
//
//            if (response.isSuccess()) {
//                PaymentAuditLogger.logWebhookProcessed("stripe", "success");
//
//                // Record monitoring metrics
//                monitoringService.stopWebhookTimer(timerSample, "stripe", "success");
//                monitoringService.recordWebhookEvent("stripe", "success", true);
//
//                return ResponseEntity.ok("Webhook processed successfully");
//            } else {
//                PaymentAuditLogger.logWebhookProcessed("stripe", "failed: " + response.getError().getMessage());
//                ApiLogger.error("Webhook processing failed: " + response.getError().getMessage());
//
//                // Record monitoring metrics
//                monitoringService.stopWebhookTimer(timerSample, "stripe", "failed");
//                monitoringService.recordWebhookEvent("stripe", "failed", false);
//
//                return ResponseEntity.badRequest().body("Webhook processing failed");
//            }
//
//        } catch (Exception e) {
//            PaymentAuditLogger.logWebhookProcessed("stripe", "error: " + e.getMessage());
//            ApiLogger.error("Error processing Stripe webhook", e);
//
//            // Record monitoring metrics
//            monitoringService.stopWebhookTimer(timerSample, "stripe", "error");
//            monitoringService.recordWebhookEvent("stripe", "error", false);
//
//            return ResponseEntity.internalServerError().body("Internal server error");
//        }
//    }
//
//    public ResponseEntity<String> webhookRateLimitFallback(
//            String payload, String signature, Exception ex) {
//        ApiLogger.warn("Webhook rate limit exceeded");
//
//        // Record rate limit hit
//        monitoringService.recordRateLimitHit("webhook", "stripe");
//
//        return ResponseEntity.status(429).body("Webhook rate limit exceeded. Please try again later.");
//    }
    
    // Rate limiting fallback methods
    
    public ResponseEntity<ApplicationResponse<PaymentResponse>> paymentRateLimitFallback(
            PaymentRequest paymentRequest, Long userId, Exception ex) {
        ApiLogger.warn("Payment processing rate limit exceeded for user: " + userId);
        ApplicationResponse<PaymentResponse> response = ApplicationResponse.error(
            429L, "Payment processing rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        return ResponseEntity.status(429).body(response);
    }
    
    public ResponseEntity<ApplicationResponse<PaymentResponse>> refundRateLimitFallback(
            Long paymentId, BigDecimal refundAmount, String reason, Long userId, Exception ex) {
        ApiLogger.warn("Refund processing rate limit exceeded for user: " + userId);
        ApplicationResponse<PaymentResponse> response = ApplicationResponse.error(
            429L, "Refund processing rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        return ResponseEntity.status(429).body(response);
    }
    
    public ResponseEntity<ApplicationResponse<PaymentResponse>> verificationRateLimitFallback(
            Long paymentId, Long userId, Exception ex) {
        ApiLogger.warn("Payment verification rate limit exceeded for user: " + userId);
        ApplicationResponse<PaymentResponse> response = ApplicationResponse.error(
            429L, "Payment verification rate limit exceeded. Please try again later.", HttpStatus.TOO_MANY_REQUESTS);
        return ResponseEntity.status(429).body(response);
    }
}