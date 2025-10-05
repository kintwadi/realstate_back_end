package com.imovel.api.services.impl;

import com.imovel.api.services.PaymentService;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.repository.SubscriptionRepository;
import com.imovel.api.repository.SubscriptionPlanRepository;
import com.imovel.api.model.Subscription;
import com.imovel.api.model.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * SQLite-specific implementation of PaymentService
 * This is a simplified implementation for development/testing with SQLite
 */
@Service
@Profile("sqlite")
@Deprecated  // use payment module instead
public class SqlitePaymentServiceImpl implements PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    @Autowired
    public SqlitePaymentServiceImpl(SubscriptionRepository subscriptionRepository,
                                   SubscriptionPlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    @Override
    public ApplicationResponse<Boolean> processPayment(Long userId, BigDecimal amount, String description) {
        try {
            ApiLogger.info("SqlitePaymentServiceImpl.processPayment",
                    "Processing payment for SQLite profile",
                    "User: " + userId + ", Amount: " + amount + ", Description: " + description);

            // For SQLite profile, we simulate successful payment processing
            // In a real implementation, this would integrate with actual payment gateways
            
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                ApiLogger.error("SqlitePaymentServiceImpl.processPayment",
                        "Invalid payment amount: " + amount);
                return ApplicationResponse.error(400L, "Invalid payment amount", null);
            }

            if (userId == null || userId <= 0) {
                ApiLogger.error("SqlitePaymentServiceImpl.processPayment",
                        "Invalid user ID: " + userId);
                return ApplicationResponse.error(400L, "Invalid user ID", null);
            }

            // Simulate payment processing delay
            Thread.sleep(100);

            ApiLogger.info("SqlitePaymentServiceImpl.processPayment",
                    "Payment processed successfully",
                    "User: " + userId + ", Amount: " + amount);

            return ApplicationResponse.success(true);

        } catch (Exception e) {
            ApiLogger.error("SqlitePaymentServiceImpl.processPayment",
                    "Error processing payment", e);
            return ApplicationResponse.error(500L, "Payment processing failed: " + e.getMessage(), null);
        }
    }

    @Override
    public ApplicationResponse<Boolean> processRefund(Long userId, BigDecimal amount, String description) {
        try {
            ApiLogger.info("SqlitePaymentServiceImpl.processRefund",
                    "Processing refund for SQLite profile",
                    "User: " + userId + ", Amount: " + amount + ", Description: " + description);

            // For SQLite profile, we simulate successful refund processing
            
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                ApiLogger.error("SqlitePaymentServiceImpl.processRefund",
                        "Invalid refund amount: " + amount);
                return ApplicationResponse.error(400L, "Invalid refund amount", null);
            }

            if (userId == null || userId <= 0) {
                ApiLogger.error("SqlitePaymentServiceImpl.processRefund",
                        "Invalid user ID: " + userId);
                return ApplicationResponse.error(400L, "Invalid user ID", null);
            }

            // Simulate refund processing delay
            Thread.sleep(100);

            ApiLogger.info("SqlitePaymentServiceImpl.processRefund",
                    "Refund processed successfully",
                    "User: " + userId + ", Amount: " + amount);

            return ApplicationResponse.success(true);

        } catch (Exception e) {
            ApiLogger.error("SqlitePaymentServiceImpl.processRefund",
                    "Error processing refund", e);
            return ApplicationResponse.error(500L, "Refund processing failed: " + e.getMessage(), null);
        }
    }

    @Override
    public ApplicationResponse<BigDecimal> calculateProratedAmount(Long subscriptionId, Long newPlanId) {
        try {
            ApiLogger.info("SqlitePaymentServiceImpl.calculateProratedAmount",
                    "Calculating prorated amount",
                    "Subscription: " + subscriptionId + ", New Plan: " + newPlanId);

            if (subscriptionId == null || subscriptionId <= 0) {
                ApiLogger.error("SqlitePaymentServiceImpl.calculateProratedAmount",
                        "Invalid subscription ID: " + subscriptionId);
                return ApplicationResponse.error(400L, "Invalid subscription ID", null);
            }

            if (newPlanId == null || newPlanId <= 0) {
                ApiLogger.error("SqlitePaymentServiceImpl.calculateProratedAmount",
                        "Invalid new plan ID: " + newPlanId);
                return ApplicationResponse.error(400L, "Invalid new plan ID", null);
            }

            // Get current subscription
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                ApiLogger.error("SqlitePaymentServiceImpl.calculateProratedAmount",
                        "Subscription not found: " + subscriptionId);
                return ApplicationResponse.error(404L, "Subscription not found", null);
            }

            // Get new plan
            Optional<SubscriptionPlan> newPlanOpt = planRepository.findById(newPlanId);
            if (newPlanOpt.isEmpty()) {
                ApiLogger.error("SqlitePaymentServiceImpl.calculateProratedAmount",
                        "New plan not found: " + newPlanId);
                return ApplicationResponse.error(404L, "New plan not found", null);
            }

            Subscription subscription = subscriptionOpt.get();
            SubscriptionPlan newPlan = newPlanOpt.get();
            SubscriptionPlan currentPlan = subscription.getPlan();

            // Calculate prorated amount based on remaining time
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = subscription.getEndDate();
            
            if (endDate.isBefore(now)) {
                // Subscription has expired
                return ApplicationResponse.success(BigDecimal.ZERO);
            }

            long remainingDays = ChronoUnit.DAYS.between(now, endDate);
            
            // Get prices based on billing cycle
            BigDecimal currentPrice = subscription.getBillingCycle().equals("monthly") 
                    ? currentPlan.getMonthlyPrice() 
                    : currentPlan.getYearlyPrice();
            
            BigDecimal newPrice = subscription.getBillingCycle().equals("monthly") 
                    ? newPlan.getMonthlyPrice() 
                    : newPlan.getYearlyPrice();

            // Calculate daily rates
            int totalDays = subscription.getBillingCycle().equals("monthly") ? 30 : 365;
            BigDecimal currentDailyRate = currentPrice.divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP);
            BigDecimal newDailyRate = newPrice.divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP);

            // Calculate prorated amounts
            BigDecimal remainingCurrentAmount = currentDailyRate.multiply(BigDecimal.valueOf(remainingDays));
            BigDecimal newAmount = newDailyRate.multiply(BigDecimal.valueOf(remainingDays));
            
            BigDecimal proratedAmount = newAmount.subtract(remainingCurrentAmount);

            ApiLogger.info("SqlitePaymentServiceImpl.calculateProratedAmount",
                    "Prorated amount calculated",
                    "Amount: " + proratedAmount + ", Remaining days: " + remainingDays);

            return ApplicationResponse.success(proratedAmount.setScale(2, RoundingMode.HALF_UP));

        } catch (Exception e) {
            ApiLogger.error("SqlitePaymentServiceImpl.calculateProratedAmount",
                    "Error calculating prorated amount", e);
            return ApplicationResponse.error(500L, "Prorated amount calculation failed: " + e.getMessage(), null);
        }
    }
}
