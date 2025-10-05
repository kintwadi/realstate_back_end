package com.imovel.api.payment.gateway;

import com.imovel.api.payment.dto.PaymentRequest;
import com.imovel.api.payment.dto.PaymentResponse;
import com.imovel.api.payment.model.Payment;
import com.imovel.api.response.ApplicationResponse;

import java.math.BigDecimal;

/**
 * Interface for payment gateway implementations
 * Each payment gateway (Stripe, PayPal, etc.) should implement this interface
 */
public interface PaymentGatewayInterface {
    
    /**
     * Process a payment through the gateway
     * 
     * @param payment The payment entity to process
     * @param userId The user ID making the payment
     * @return ApplicationResponse containing the payment result
     */
    ApplicationResponse<PaymentResponse> processPayment(Payment payment, Long userId);
    
    /**
     * Process a refund through the gateway
     * 
     * @param payment The original payment to refund
     * @param refundAmount The amount to refund (can be partial)
     * @param reason The reason for the refund
     * @return ApplicationResponse containing the refund result
     */
    ApplicationResponse<PaymentResponse> processRefund(Payment payment, BigDecimal refundAmount, String reason);
    
    /**
     * Verify a payment status with the gateway
     * 
     * @param gatewayPaymentId The payment ID from the gateway
     * @return ApplicationResponse containing the payment status
     */
    ApplicationResponse<PaymentResponse> verifyPaymentStatus(String gatewayPaymentId);
    
    /**
     * Cancel a pending payment
     * 
     * @param payment The payment to cancel
     * @return ApplicationResponse containing the cancellation result
     */
    ApplicationResponse<PaymentResponse> cancelPayment(Payment payment);
    
    /**
     * Handle webhook notifications from the gateway
     * 
     * @param webhookPayload The webhook payload from the gateway
     * @param signature The webhook signature for verification
     * @return ApplicationResponse indicating webhook processing result
     */
    ApplicationResponse<String> handleWebhook(String webhookPayload, String signature);
    
    /**
     * Get the gateway name
     * 
     * @return The name of the payment gateway
     */
    String getGatewayName();
    
    /**
     * Check if the gateway supports the given currency
     * 
     * @param currency The currency code to check
     * @return true if supported, false otherwise
     */
    boolean supportsCurrency(String currency);
    
    /**
     * Get the minimum amount supported by the gateway for the given currency
     * 
     * @param currency The currency code
     * @return The minimum amount
     */
    BigDecimal getMinimumAmount(String currency);
}
