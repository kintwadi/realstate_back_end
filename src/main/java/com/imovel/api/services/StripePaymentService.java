package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.Subscription;
import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.repository.SubscriptionPlanRepository;
import com.imovel.api.repository.SubscriptionRepository;
import com.imovel.api.response.ApplicationResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
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

    @Autowired
    public StripePaymentService(SubscriptionRepository subscriptionRepository,
                              SubscriptionPlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    @Override
    public ApplicationResponse<Boolean> processPayment(Long userId, BigDecimal amount, String description) {
        try {
            // In a real implementation, this would call Stripe API
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ApplicationResponse.success(true, "No payment needed");
            }

            // Mock Stripe API call
            boolean paymentSuccess = mockStripeCharge(userId, amount, description); // to do
            
            if (paymentSuccess) {
                return ApplicationResponse.success(true, "Payment processed successfully");
            } else {
                return ApplicationResponse.error(
                    ApiCode.SUBSCRIPTION_PAYMENT_FAILED.getCode(),
                    "Payment processing failed",
                    ApiCode.SUBSCRIPTION_PAYMENT_FAILED.getHttpStatus()
                );
            }
        } catch (Exception e) {
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

            // Mock Stripe refund
            boolean refundSuccess = mockStripeRefund(userId, amount, description);

            if (refundSuccess) {
                return ApplicationResponse.success(true, "Refund processed successfully");
            } else {
                return ApplicationResponse.error(
                        ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                        "Refund processing failed",
                        ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
                );
            }
        } catch (Exception e) {
            return ApplicationResponse.error(
                    ApiCode.PAYMENT_GATEWAY_ERROR.getCode(),
                    "Refund processing error: " + e.getMessage(),
                    ApiCode.PAYMENT_GATEWAY_ERROR.getHttpStatus()
            );
        }
    }

    // Mock Stripe refund processing
    private boolean mockStripeRefund(Long userId, BigDecimal amount, String description) {
        try {
            Stripe.apiKey = "your_stripe_secret_key";

            // Get the payment intent ID from  database
            String paymentIntentId = getLastPaymentIntentId(userId);

            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put("payment_intent", paymentIntentId);
            refundParams.put("amount", amount.multiply(new BigDecimal("100")).longValue());

            Refund.create(refundParams);
            return true;
        } catch (StripeException e) {
            ApiLogger.error("Stripe refund failed", e);
            return false;
        }
    }
    private String getLastPaymentIntentId(Long userId) {
        // Query  database to get the last payment intent ID
        return "pi_123"; // Example
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

    // Mock Stripe payment processing
    private boolean mockStripeCharge(Long userId, BigDecimal amount, String description) {
        // In a real implementation, this would call Stripe API:
        // Stripe.apiKey = "your_stripe_api_key";
        // Map<String, Object> params = new HashMap<>();
        // params.put("amount", amount.multiply(100).longValue()); // in cents
        // params.put("currency", "usd");
        // params.put("customer", getStripeCustomerId(userId));
        // params.put("description", description);
        
        // Charge charge = Charge.create(params);
        // return charge.getPaid();
        
        // For demo purposes, just return true
        System.out.printf("Mock Stripe charge: User %d, Amount %s, Desc: %s%n", 
            userId, amount, description);
        return true;
    }
}