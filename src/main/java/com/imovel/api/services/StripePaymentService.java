package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.Subscription;
import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.payment.stripe.config.StripeConfig;
import com.imovel.api.repository.SubscriptionPlanRepository;
import com.imovel.api.repository.SubscriptionRepository;
import com.imovel.api.response.ApplicationResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripePaymentService implements PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final StripeConfig stripeConfig;

    @Autowired
    public StripePaymentService(SubscriptionRepository subscriptionRepository,
                              SubscriptionPlanRepository planRepository,
                              StripeConfig stripeConfig) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.stripeConfig = stripeConfig;
    }

    @Override
    public ApplicationResponse<Boolean> processPayment(Long userId, BigDecimal amount, String description) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ApplicationResponse.success(true, "No payment needed");
            }

            // Create PaymentIntent with Stripe
            PaymentIntent paymentIntent = createPaymentIntent(userId, amount, description);
            
            if (paymentIntent != null && "succeeded".equals(paymentIntent.getStatus())) {
                ApiLogger.info("Payment processed successfully for user: " + userId + 
                             ", PaymentIntent ID: " + paymentIntent.getId());
                return ApplicationResponse.success(true, "Payment processed successfully");
            } else {
                ApiLogger.error("Payment failed for user: " + userId + 
                              ", PaymentIntent status: " + (paymentIntent != null ? paymentIntent.getStatus() : "null"));
                return ApplicationResponse.error(
                    ApiCode.SUBSCRIPTION_PAYMENT_FAILED.getCode(),
                    "Payment processing failed",
                    ApiCode.SUBSCRIPTION_PAYMENT_FAILED.getHttpStatus()
                );
            }
        } catch (StripeException e) {
            ApiLogger.error("Stripe API error during payment processing", e);
            return ApplicationResponse.error(
                ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                "Stripe payment error: " + e.getMessage(),
                ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error("Unexpected error during payment processing", e);
            return ApplicationResponse.error(
                ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                "Payment processing error: " + e.getMessage(),
                ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
            );
        }
    }

    @Override
    public ApplicationResponse<Boolean> processRefund(Long userId, BigDecimal amount, String description) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ApplicationResponse.success(true, "No refund needed");
            }

            // Get the payment intent ID from database
            String paymentIntentId = getLastPaymentIntentId(userId);
            if (paymentIntentId == null) {
                return ApplicationResponse.error(
                    ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                    "No payment found for refund",
                    ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
                );
            }

            // Create refund with Stripe
            Refund refund = createRefund(paymentIntentId, amount, description);

            if (refund != null && "succeeded".equals(refund.getStatus())) {
                ApiLogger.info("Refund processed successfully for user: " + userId + 
                             ", Refund ID: " + refund.getId());
                return ApplicationResponse.success(true, "Refund processed successfully");
            } else {
                ApiLogger.error("Refund failed for user: " + userId + 
                              ", Refund status: " + (refund != null ? refund.getStatus() : "null"));
                return ApplicationResponse.error(
                    ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                    "Refund processing failed",
                    ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
                );
            }
        } catch (StripeException e) {
            ApiLogger.error("Stripe API error during refund processing", e);
            return ApplicationResponse.error(
                ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                "Stripe refund error: " + e.getMessage(),
                ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error("Unexpected error during refund processing", e);
            return ApplicationResponse.error(
                ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                "Refund processing error: " + e.getMessage(),
                ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
            );
        }
    }

    /**
     * Creates a PaymentIntent with Stripe
     */
    private PaymentIntent createPaymentIntent(Long userId, BigDecimal amount, String description) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amount.multiply(new BigDecimal("100")).longValue()) // Convert to cents
            .setCurrency("usd")
            .setDescription(description)
            .putMetadata("userId", userId.toString())
            .setConfirm(true)
            .setPaymentMethod("pm_card_visa") // For testing - in production, this would come from frontend
            .setReturnUrl("https://your-website.com/return") // Configure your return URL
            .build();

        return PaymentIntent.create(params);
    }

    /**
     * Creates a refund with Stripe
     */
    private Refund createRefund(String paymentIntentId, BigDecimal amount, String reason) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
            .setPaymentIntent(paymentIntentId)
            .setAmount(amount.multiply(new BigDecimal("100")).longValue()) // Convert to cents
            .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
            .putMetadata("reason", reason)
            .build();

        return Refund.create(params);
    }

    /**
     * Retrieves the last payment intent ID for a user from database
     * TODO: Implement proper database query to get the actual payment intent ID
     */
    private String getLastPaymentIntentId(Long userId) {
        // This should query your database to get the actual payment intent ID
        // For now, returning null to indicate no payment found
        // In production, implement proper database lookup
        ApiLogger.error("getLastPaymentIntentId not implemented - returning null for user: " + userId);
        return null;
    }

    @Override
    public ApplicationResponse<BigDecimal> calculateProratedAmount(Long subscriptionId, Long newPlanId) {
        try {
            Subscription currentSub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
            
            SubscriptionPlan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new IllegalArgumentException("New plan not found"));

            // Calculate remaining time in current period
            long totalDays = ChronoUnit.DAYS.between(currentSub.getStartDate(), currentSub.getEndDate());
            long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), currentSub.getEndDate());
            
            // Calculate unused value of current plan
            BigDecimal currentPlanDailyRate = getDailyRate(currentSub.getPlan(), currentSub.getBillingCycle());
            BigDecimal unusedValue = currentPlanDailyRate.multiply(BigDecimal.valueOf(remainingDays));
            
            // Calculate cost of new plan for remaining period
            BigDecimal newPlanDailyRate = getDailyRate(newPlan, currentSub.getBillingCycle());
            BigDecimal newPlanCost = newPlanDailyRate.multiply(BigDecimal.valueOf(remainingDays));
            
            // Amount due (positive = charge, negative = credit)
            BigDecimal amountDue = newPlanCost.subtract(unusedValue);
            
            return ApplicationResponse.success(amountDue.setScale(2, RoundingMode.HALF_UP));
        } catch (IllegalArgumentException e) {
            return ApplicationResponse.error(
                ApiCode.SUBSCRIPTION_NOT_FOUND.getCode(),
                e.getMessage(),
                ApiCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            return ApplicationResponse.error(
                ApiCode.SYSTEM_ERROR.getCode(),
                "Failed to calculate prorated amount: " + e.getMessage(),
                ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    private BigDecimal getDailyRate(SubscriptionPlan plan, String billingCycle) {
        BigDecimal planPrice = billingCycle.equals("monthly") ? plan.getMonthlyPrice() : plan.getYearlyPrice();
        int daysInPeriod = billingCycle.equals("monthly") ? 30 : 365; // Approximation
        return planPrice.divide(BigDecimal.valueOf(daysInPeriod), 10, RoundingMode.HALF_UP);
    }


}