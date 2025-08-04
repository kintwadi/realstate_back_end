package com.imovel.api.services;
import com.imovel.api.error.ApiCode;
import com.imovel.api.model.Subscription;
import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.payment.PaymentResponse;
import com.imovel.api.repository.SubscriptionPlanRepository;
import com.imovel.api.repository.SubscriptionRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.response.SubscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final IPaymentService paymentService;

    @Autowired
    public SubscriptionService(SubscriptionPlanRepository planRepository,
                               SubscriptionRepository subscriptionRepository,
                               IPaymentService paymentService) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentService = paymentService;
    }

    // Create new subscription
    @Transactional
    public ApplicationResponse<SubscriptionResponse> subscribeUser(Long userId,
                                                                   Long planId,
                                                                   String billingCycle,
                                                                   String currency) {
        try {
            // Validate billing cycle
            if (!billingCycle.equals("monthly") && !billingCycle.equals("yearly")) {
                ApiLogger.error("SubscriptionService.subscribeUser",
                        "Invalid billing cycle. Must be 'monthly' or 'yearly'");
                return ApplicationResponse.error(
                        ApiCode.INVALID_SUBSCRIPTION_DATA.getCode(),
                        "Invalid billing cycle. Must be 'monthly' or 'yearly'",
                        ApiCode.INVALID_SUBSCRIPTION_DATA.getHttpStatus()
                );
            }

            Optional<Subscription> subscriptionOptional = subscriptionRepository.findByUserIdAndPlanId(userId, planId);

            if(subscriptionOptional.isPresent()){
                ApiLogger.error("SubscriptionService.subscribeUser",
                        "subscription already exist for this user and plan");
                return ApplicationResponse.error(
                        ApiCode.SUBSCRIPTION_ALREADY_EXISTS.getCode(),
                        "subscription already exist for this user and plan",
                        ApiCode.SUBSCRIPTION_ALREADY_EXISTS.getHttpStatus()
                );
            }
            SubscriptionPlan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid plan ID"));

            // Process payment for new subscription
            BigDecimal amount = billingCycle.equals("monthly") ? plan.getMonthlyPrice() : plan.getYearlyPrice();
            ApiLogger.debug("SubscriptionService.subscribeUser",
                    "Processing payment for new subscription",
                    "User: " + userId + ", Amount: " + amount);

            ApplicationResponse<PaymentResponse> paymentResponse = paymentService.processPayment(
                    userId,
                    amount,
                    currency,
                    "New subscription - " + plan.getName()
            );

            if (!paymentResponse.isSuccess()) {
                ApiLogger.error("SubscriptionService.subscribeUser",
                        "Payment processing failed", paymentResponse.getError());
                return ApplicationResponse.error(
                        paymentResponse.getError().getCode(),
                        paymentResponse.getError().getMessage(),
                        paymentResponse.getError().getStatus()
                );
            }

            // Create subscription
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = billingCycle.equals("monthly") ?
                    now.plusMonths(1) : now.plusYears(1);

            Subscription subscription = new Subscription();
            subscription.setPlan(plan);
            subscription.setUserId(userId);
            subscription.setBillingCycle(billingCycle);
            subscription.setStartDate(now);
            subscription.setEndDate(endDate);
            subscription.setStatus("active");

            Subscription savedSubscription = subscriptionRepository.save(subscription);
            ApiLogger.info("SubscriptionService.subscribeUser",
                    "Subscription created successfully", savedSubscription);
            return ApplicationResponse.success(SubscriptionResponse.parse(savedSubscription), "Subscription created successfully");
        } catch (IllegalArgumentException e) {
            ApiLogger.error("SubscriptionService.subscribeUser",
                    "Invalid subscription plan", e);
            return ApplicationResponse.error(
                    ApiCode.INVALID_SUBSCRIPTION_PLAN.getCode(),
                    e.getMessage(),
                    ApiCode.INVALID_SUBSCRIPTION_PLAN.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error("SubscriptionService.subscribeUser",
                    "Failed to create subscription", e);
            return ApplicationResponse.error(
                    ApiCode.SUBSCRIPTION_PAYMENT_FAILED.getCode(),
                    "Failed to create subscription: " + e.getMessage(),
                    ApiCode.SUBSCRIPTION_PAYMENT_FAILED.getHttpStatus()
            );
        }
    }

    // Get user's active subscriptions
    public ApplicationResponse<List<SubscriptionResponse>> getUserSubscriptions(Long userId) {
        try {
            List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
            if (subscriptions.isEmpty()) {
                ApiLogger.debug("SubscriptionService.getUserSubscriptions",
                        "No subscriptions found for user", userId);
                return ApplicationResponse.success(SubscriptionResponse.parse(subscriptions), "No subscriptions found for this user");
            }
            ApiLogger.info("SubscriptionService.getUserSubscriptions",
                    "User subscriptions retrieved", subscriptions.size());
            return ApplicationResponse.success(SubscriptionResponse.parse(subscriptions), "User subscriptions retrieved successfully");
        } catch (Exception e) {
            ApiLogger.error("SubscriptionService.getUserSubscriptions",
                    "Failed to retrieve user subscriptions", e);
            return ApplicationResponse.error(
                    ApiCode.DATABASE_CONNECTION_ERROR.getCode(),
                    "Failed to retrieve user subscriptions: " + e.getMessage(),
                    ApiCode.DATABASE_CONNECTION_ERROR.getHttpStatus()
            );
        }
    }

    // Cancel subscription
    @Transactional
    public ApplicationResponse<SubscriptionResponse> cancelSubscription(Long subscriptionId,String currency) {
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid subscription ID"));

            if (subscription.getStatus().equals("canceled")) {
                ApiLogger.debug("SubscriptionService.cancelSubscription",
                        "Subscription is already canceled", subscriptionId);
                return ApplicationResponse.error(
                        ApiCode.INVALID_SUBSCRIPTION_DATA.getCode(),
                        "Subscription is already canceled",
                        ApiCode.INVALID_SUBSCRIPTION_DATA.getHttpStatus()
                );
            }

            // Calculate refund for unused period (if applicable)
            BigDecimal refundAmount = calculateRefundAmount(subscription);
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                ApiLogger.debug("SubscriptionService.cancelSubscription",
                        "Processing refund for subscription cancellation",
                        "Amount: " + refundAmount);

                ApplicationResponse<PaymentResponse> refundResponse = paymentService.processRefund(
                        subscription.getUserId(),
                        refundAmount,
                        currency,
                        "Subscription cancellation refund"
                );

                if (!refundResponse.isSuccess()) {
                    ApiLogger.error("SubscriptionService.cancelSubscription",
                            "Refund processing failed", refundResponse.getError());
                    return ApplicationResponse.error(
                            refundResponse.getError().getCode(),
                            "Subscription canceled but refund failed: " + refundResponse.getError().getMessage(),
                            refundResponse.getError().getStatus()
                    );
                }
            }

            subscription.setStatus("canceled");
            Subscription updatedSubscription = subscriptionRepository.save(subscription);

            String message = refundAmount.compareTo(BigDecimal.ZERO) > 0 ?
                    "Subscription canceled successfully. Refund of " + refundAmount + " processed." :
                    "Subscription canceled successfully. No refund applicable.";

            ApiLogger.info("SubscriptionService.cancelSubscription",
                    message, updatedSubscription);
            return ApplicationResponse.success(SubscriptionResponse.parse(updatedSubscription), message);
        } catch (IllegalArgumentException e) {
            ApiLogger.error("SubscriptionService.cancelSubscription",
                    "Invalid subscription ID", e);
            return ApplicationResponse.error(
                    ApiCode.SUBSCRIPTION_NOT_FOUND.getCode(),
                    e.getMessage(),
                    ApiCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error("SubscriptionService.cancelSubscription",
                    "Failed to cancel subscription", e);
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to cancel subscription: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    // Change subscription plan (upgrade/downgrade)
    @Transactional
    public ApplicationResponse<SubscriptionResponse> changePlan(Long subscriptionId, Long newPlanId,String currency, boolean immediate) {
        try {
            // Get current subscription
            Subscription currentSub = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

            // Get new plan
            SubscriptionPlan newPlan = planRepository.findById(newPlanId)
                    .orElseThrow(() -> new IllegalArgumentException("New plan not found"));

            // Check if changing to same plan
            if (currentSub.getPlan().getId().equals(newPlanId)) {
                ApiLogger.debug("SubscriptionService.changePlan",
                        "Attempt to change to the same plan",
                        "Subscription: " + subscriptionId + ", Plan: " + newPlanId);
                return ApplicationResponse.error(
                        ApiCode.INVALID_SUBSCRIPTION_DATA.getCode(),
                        "Cannot change to the same plan",
                        ApiCode.INVALID_SUBSCRIPTION_DATA.getHttpStatus()
                );
            }

            // Calculate prorated amount
            ProrationResult proration = calculateProration(currentSub, newPlan, immediate);

            // Process payment if needed
            if (proration.getAmountDue().compareTo(BigDecimal.ZERO) > 0) {
                ApiLogger.debug("SubscriptionService.changePlan",
                        "Processing payment for plan change",
                        "Amount: " + proration.getAmountDue());

                ApplicationResponse<PaymentResponse> paymentResponse = paymentService.processPayment(
                        currentSub.getUserId(),
                        proration.getAmountDue(),
                        currency,
                        "Subscription plan change to " + newPlan.getName()
                );

                if (!paymentResponse.isSuccess()) {
                    ApiLogger.error("SubscriptionService.changePlan",
                            "Payment processing failed", paymentResponse.getError());
                    return ApplicationResponse.error(
                            paymentResponse.getError().getCode(),
                            paymentResponse.getError().getMessage(),
                            paymentResponse.getError().getStatus()
                    );
                }
            }

            // Update subscription
            currentSub.setPlan(newPlan);
            currentSub.setStartDate(proration.getNewStartDate());
            currentSub.setEndDate(proration.getNewEndDate());

            if (immediate) {
                currentSub.setStatus("active");
            }

            Subscription updatedSub = subscriptionRepository.save(currentSub);

            String resultMessage = "Subscription plan changed successfully. " +
                    (proration.getAmountDue().compareTo(BigDecimal.ZERO) > 0 ?
                            "Prorated amount charged: " + proration.getAmountDue() :
                            "Credit applied: " + proration.getAmountDue().abs());

            ApiLogger.info("SubscriptionService.changePlan",
                    resultMessage, updatedSub);
            return ApplicationResponse.success(SubscriptionResponse.parse(updatedSub), resultMessage);

        } catch (IllegalArgumentException e) {
            ApiLogger.error("SubscriptionService.changePlan",
                    "Subscription or plan not found", e);
            return ApplicationResponse.error(
                    ApiCode.SUBSCRIPTION_NOT_FOUND.getCode(),
                    e.getMessage(),
                    ApiCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error("SubscriptionService.changePlan",
                    "Failed to change subscription plan", e);
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to change subscription plan: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    // Calculate prorated amount for plan change
    public ApplicationResponse<BigDecimal> calculatePlanChange(Long subscriptionId, Long newPlanId) {
        try {
            Subscription currentSub = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

            SubscriptionPlan newPlan = planRepository.findById(newPlanId)
                    .orElseThrow(() -> new IllegalArgumentException("New plan not found"));

            ProrationResult proration = calculateProration(currentSub, newPlan, true);

            String message = proration.getAmountDue().compareTo(BigDecimal.ZERO) > 0 ?
                    "Upgrade requires payment of " + proration.getAmountDue() :
                    "Downgrade will credit " + proration.getAmountDue().abs();

            ApiLogger.debug("SubscriptionService.calculatePlanChange",
                    "Calculated proration amount", message);
            return ApplicationResponse.success(
                    proration.getAmountDue().setScale(2, RoundingMode.HALF_UP),
                    message
            );
        } catch (IllegalArgumentException e) {
            ApiLogger.error("SubscriptionService.calculatePlanChange",
                    "Subscription or plan not found", e);
            return ApplicationResponse.error(
                    ApiCode.SUBSCRIPTION_NOT_FOUND.getCode(),
                    e.getMessage(),
                    ApiCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus()
            );
        } catch (Exception e) {
            ApiLogger.error("SubscriptionService.calculatePlanChange",
                    "Failed to calculate prorated amount", e);
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to calculate prorated amount: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    // Helper method to calculate proration
    private ProrationResult calculateProration(Subscription currentSub, SubscriptionPlan newPlan, boolean immediate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newStartDate = immediate ? now : currentSub.getStartDate();

        // Calculate remaining time in current period
        long totalDays = ChronoUnit.DAYS.between(currentSub.getStartDate(), currentSub.getEndDate());
        long remainingDays = ChronoUnit.DAYS.between(now, currentSub.getEndDate());

        // Calculate unused value of current plan (prorated)
        BigDecimal currentPlanDailyRate = getDailyRate(currentSub.getPlan(), currentSub.getBillingCycle());
        BigDecimal unusedValue = currentPlanDailyRate.multiply(BigDecimal.valueOf(remainingDays));

        // Calculate cost of new plan for remaining period
        BigDecimal newPlanDailyRate = getDailyRate(newPlan, currentSub.getBillingCycle());
        BigDecimal newPlanCost = newPlanDailyRate.multiply(BigDecimal.valueOf(remainingDays));

        // Calculate amount due (positive = charge customer, negative = credit)
        BigDecimal amountDue = newPlanCost.subtract(unusedValue);

        // Calculate new end date
        LocalDateTime newEndDate;
        if (immediate) {
            // For immediate changes, start new period from now
            if (currentSub.getBillingCycle().equals("monthly")) {
                newEndDate = now.plusMonths(1);
            } else {
                newEndDate = now.plusYears(1);
            }
        } else {
            // For non-immediate changes, keep original end date
            newEndDate = currentSub.getEndDate();
        }

        return new ProrationResult(amountDue, newStartDate, newEndDate);
    }

    // Helper method to calculate refund amount
    private BigDecimal calculateRefundAmount(Subscription subscription) {
        if (!subscription.getStatus().equals("active")) {
            return BigDecimal.ZERO;
        }

        long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());
        if (remainingDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyRate = getDailyRate(subscription.getPlan(), subscription.getBillingCycle());
        return dailyRate.multiply(BigDecimal.valueOf(remainingDays))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Helper method to calculate daily rate
    private BigDecimal getDailyRate(SubscriptionPlan plan, String billingCycle) {
        BigDecimal planPrice = billingCycle.equals("monthly") ? plan.getMonthlyPrice() : plan.getYearlyPrice();
        int daysInPeriod = billingCycle.equals("monthly") ? 30 : 365; // Approximation
        return planPrice.divide(BigDecimal.valueOf(daysInPeriod), 10, RoundingMode.HALF_UP);
    }

    // Helper class for proration results
    private static class ProrationResult {
        private final BigDecimal amountDue;
        private final LocalDateTime newStartDate;
        private final LocalDateTime newEndDate;

        public ProrationResult(BigDecimal amountDue, LocalDateTime newStartDate, LocalDateTime newEndDate) {
            this.amountDue = amountDue;
            this.newStartDate = newStartDate;
            this.newEndDate = newEndDate;
        }

        public BigDecimal getAmountDue() {
            return amountDue;
        }

        public LocalDateTime getNewStartDate() {
            return newStartDate;
        }

        public LocalDateTime getNewEndDate() {
            return newEndDate;
        }
    }
}