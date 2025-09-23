package com.imovel.api.payment.controller;

import com.imovel.api.logger.ApiLogger;
import com.imovel.api.payment.dto.PaymentRequest;
import com.imovel.api.payment.dto.PaymentResponse;
import com.imovel.api.payment.service.PaymentService;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    /**
     * Process a new payment
     */
    @PostMapping("/process")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> processPayment(
            @RequestBody PaymentRequest paymentRequest,
            @RequestParam Long userId)
    {
        
        ApiLogger.info("Processing payment request for user: " + userId);
        
        ApplicationResponse<PaymentResponse> response = paymentService.processPayment(paymentRequest, userId);
        
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
    public ResponseEntity<ApplicationResponse<Page<PaymentResponse>>> getUserPayments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        ApiLogger.info("Retrieving payments for user: " + userId);
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        ApplicationResponse<Page<PaymentResponse>> response = paymentService.getUserPayments(userId, pageable);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }
    
    /**
     * Process a refund
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> processRefund(
            @PathVariable Long paymentId,
            @RequestParam BigDecimal refundAmount,
            @RequestParam(required = false) String reason,
            @RequestParam Long userId) {
        
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
}