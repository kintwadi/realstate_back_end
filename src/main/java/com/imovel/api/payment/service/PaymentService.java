package com.imovel.api.payment.service;

import com.imovel.api.payment.dto.PaymentRequest;
import com.imovel.api.payment.dto.PaymentResponse;
import com.imovel.api.payment.model.Payment;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.pagination.Pagination;
import com.imovel.api.pagination.PaginationResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Main payment service interface
 */
public interface PaymentService {
    
    /**
     * Process a payment request
     * 
     * @param paymentRequest The payment request details
     * @param userId The user ID making the payment
     * @return ApplicationResponse containing the payment result
     */
    ApplicationResponse<PaymentResponse> processPayment(PaymentRequest paymentRequest, Long userId);
    
    /**
     * Get payment by ID
     * 
     * @param paymentId The payment ID
     * @param userId The user ID (for authorization)
     * @return ApplicationResponse containing the payment details
     */
    ApplicationResponse<PaymentResponse> getPaymentById(Long paymentId, Long userId);
    
    /**
     * Get user's payment history with pagination
     */
    ApplicationResponse<PaginationResult<PaymentResponse>> getUserPayments(Long userId, Pagination pagination, String sortBy, String sortDirection);
    
    /**
     * Process a refund
     * 
     * @param paymentId The original payment ID
     * @param refundAmount The amount to refund
     * @param reason The reason for refund
     * @param userId The user ID (for authorization)
     * @return ApplicationResponse containing the refund result
     */
    ApplicationResponse<PaymentResponse> processRefund(Long paymentId, BigDecimal refundAmount, String reason, Long userId);
    
    /**
     * Cancel a pending payment
     * 
     * @param paymentId The payment ID to cancel
     * @param userId The user ID (for authorization)
     * @return ApplicationResponse containing the cancellation result
     */
    ApplicationResponse<PaymentResponse> cancelPayment(Long paymentId, Long userId);
    
    /**
     * Verify payment status with the gateway
     * 
     * @param paymentId The payment ID
     * @param userId The user ID (for authorization)
     * @return ApplicationResponse containing the updated payment status
     */
    ApplicationResponse<PaymentResponse> verifyPaymentStatus(Long paymentId, Long userId);
    
    /**
     * Get payment statistics for a user
     * 
     * @param userId The user ID
     * @param startDate Start date for statistics
     * @param endDate End date for statistics
     * @return ApplicationResponse containing payment statistics
     */
    ApplicationResponse<PaymentStatistics> getPaymentStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Handle webhook from payment gateway
     * 
     * @param gateway The payment gateway name
     * @param webhookPayload The webhook payload
     * @param signature The webhook signature
     * @return ApplicationResponse indicating webhook processing result
     */
    ApplicationResponse<String> handleWebhook(String gateway, String webhookPayload, String signature);
    
    /**
     * Inner class for payment statistics
     */
    class PaymentStatistics {
        private BigDecimal totalAmount;
        private BigDecimal successfulAmount;
        private BigDecimal failedAmount;
        private BigDecimal refundedAmount;
        private long totalCount;
        private long successfulCount;
        private long failedCount;
        private long refundedCount;
        
        // Constructors
        public PaymentStatistics() {}
        
        public PaymentStatistics(BigDecimal totalAmount, BigDecimal successfulAmount, 
                               BigDecimal failedAmount, BigDecimal refundedAmount,
                               long totalCount, long successfulCount, 
                               long failedCount, long refundedCount) {
            this.totalAmount = totalAmount;
            this.successfulAmount = successfulAmount;
            this.failedAmount = failedAmount;
            this.refundedAmount = refundedAmount;
            this.totalCount = totalCount;
            this.successfulCount = successfulCount;
            this.failedCount = failedCount;
            this.refundedCount = refundedCount;
        }
        
        // Getters and Setters
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public BigDecimal getSuccessfulAmount() { return successfulAmount; }
        public void setSuccessfulAmount(BigDecimal successfulAmount) { this.successfulAmount = successfulAmount; }
        
        public BigDecimal getFailedAmount() { return failedAmount; }
        public void setFailedAmount(BigDecimal failedAmount) { this.failedAmount = failedAmount; }
        
        public BigDecimal getRefundedAmount() { return refundedAmount; }
        public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }
        
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        
        public long getSuccessfulCount() { return successfulCount; }
        public void setSuccessfulCount(long successfulCount) { this.successfulCount = successfulCount; }
        
        public long getFailedCount() { return failedCount; }
        public void setFailedCount(long failedCount) { this.failedCount = failedCount; }
        
        public long getRefundedCount() { return refundedCount; }
        public void setRefundedCount(long refundedCount) { this.refundedCount = refundedCount; }
    }
}
