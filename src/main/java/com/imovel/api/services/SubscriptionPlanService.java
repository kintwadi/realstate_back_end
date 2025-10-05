package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.repository.SubscriptionPlanRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.SubscriptionPlanResponse;
import com.imovel.api.util.SubscriptionPlanInitializer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    // @PostConstruct
    // public void init() {
    //     try {
    //         SubscriptionPlanInitializer.initializeDefaultSubscriptionPlan(subscriptionPlanRepository);
    //         ApiLogger.info("SubscriptionPlanService", "Default subscription plans initialized successfully");
    //     } catch (Exception e) {
    //         ApiLogger.error("SubscriptionPlanService", "Failed to initialize default subscription plans", e);
    //     }
    // }

    public ApplicationResponse<SubscriptionPlanResponse> createSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        ApiLogger.info("SubscriptionPlanService.createSubscriptionPlan", "Creating new subscription plan", subscriptionPlan);
        try {
            SubscriptionPlan savedPlan = subscriptionPlanRepository.save(subscriptionPlan);
            SubscriptionPlanResponse response = SubscriptionPlanResponse.parse(savedPlan);
            ApiLogger.info("SubscriptionPlanService.createSubscriptionPlan", "Subscription plan created successfully", response);
            return ApplicationResponse.success(response, "Subscription plan created successfully");
        } catch (Exception e) {
            ApiLogger.error("SubscriptionPlanService.createSubscriptionPlan", "Failed to create subscription plan", e, subscriptionPlan);
            return ApplicationResponse.error(ApiCode.SUBSCRIPTION_ALREADY_EXISTS.getCode(),
                    "Failed to create subscription plan: " + e.getMessage(), 
                    ApiCode.SUBSCRIPTION_ALREADY_EXISTS.getHttpStatus());
        }
    }

    public ApplicationResponse<SubscriptionPlanResponse> getSubscriptionPlanById(Long id) {
        ApiLogger.info("SubscriptionPlanService.getSubscriptionPlanById", "Fetching subscription plan by ID", id);
        try {
            Optional<SubscriptionPlan> planOptional = subscriptionPlanRepository.findById(id);
            if (planOptional.isPresent()) {
                SubscriptionPlanResponse response = SubscriptionPlanResponse.parse(planOptional.get());
                ApiLogger.info("SubscriptionPlanService.getSubscriptionPlanById", "Subscription plan found", response);
                return ApplicationResponse.success(response);
            } else {
                ApiLogger.error("SubscriptionPlanService.getSubscriptionPlanById", "Subscription plan not found", id);
                return ApplicationResponse.error(ApiCode.SUBSCRIPTION_NOT_FOUND.getCode(), 
                        "Subscription plan not found with ID: " + id, 
                        ApiCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            ApiLogger.error("SubscriptionPlanService.getSubscriptionPlanById", "Failed to fetch subscription plan", e, id);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                    "Failed to fetch subscription plan: " + e.getMessage(), 
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    public ApplicationResponse<List<SubscriptionPlanResponse>> getAllSubscriptionPlans() {
        ApiLogger.info("SubscriptionPlanService.getAllSubscriptionPlans", "Fetching all subscription plans");
        try {
            List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllByOrderByIdAsc();
            List<SubscriptionPlanResponse> responses = SubscriptionPlanResponse.parse(plans);
            ApiLogger.info("SubscriptionPlanService.getAllSubscriptionPlans", "Successfully fetched all subscription plans", responses.size());
            return ApplicationResponse.success(responses);
        } catch (Exception e) {
            ApiLogger.error("SubscriptionPlanService.getAllSubscriptionPlans", "Failed to fetch subscription plans", e);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                    "Failed to fetch subscription plans: " + e.getMessage(), 
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    public ApplicationResponse<SubscriptionPlanResponse> updateSubscriptionPlan(Long id, SubscriptionPlan updatedPlan) {
        ApiLogger.info("SubscriptionPlanService.updateSubscriptionPlan", "Updating subscription plan", updatedPlan);
        try {
            Optional<SubscriptionPlan> existingPlanOptional = subscriptionPlanRepository.findById(id);
            if (existingPlanOptional.isPresent()) {
                SubscriptionPlan existingPlan = existingPlanOptional.get();
                
                // Update fields
                existingPlan.setName(updatedPlan.getName());
                existingPlan.setDescription(updatedPlan.getDescription());
                existingPlan.setMonthlyPrice(updatedPlan.getMonthlyPrice());
                existingPlan.setYearlyPrice(updatedPlan.getYearlyPrice());
                existingPlan.setListingLimit(updatedPlan.getListingLimit());
                existingPlan.setAvailabilityDays(updatedPlan.getAvailabilityDays());
                existingPlan.setFeatured(updatedPlan.getFeatured());
                existingPlan.setSupportType(updatedPlan.getSupportType());
                
                SubscriptionPlan savedPlan = subscriptionPlanRepository.save(existingPlan);
                SubscriptionPlanResponse response = SubscriptionPlanResponse.parse(savedPlan);
                ApiLogger.info("SubscriptionPlanService.updateSubscriptionPlan", "Subscription plan updated successfully", response);
                return ApplicationResponse.success(response, "Subscription plan updated successfully");
            } else {
                ApiLogger.error("SubscriptionPlanService.updateSubscriptionPlan", "Subscription plan not found", id);
                return ApplicationResponse.error(ApiCode.SUBSCRIPTION_NOT_FOUND.getCode(), 
                        "Subscription plan not found with ID: " + id, 
                        ApiCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            ApiLogger.error("SubscriptionPlanService.updateSubscriptionPlan", "Failed to update subscription plan", e, updatedPlan);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                    "Failed to update subscription plan: " + e.getMessage(), 
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    public ApplicationResponse<Void> deleteSubscriptionPlan(Long id) {
        ApiLogger.info("SubscriptionPlanService.deleteSubscriptionPlan", "Deleting subscription plan", id);
        try {
            Optional<SubscriptionPlan> planOptional = subscriptionPlanRepository.findById(id);
            if (planOptional.isPresent()) {
                subscriptionPlanRepository.deleteById(id);
                ApiLogger.info("SubscriptionPlanService.deleteSubscriptionPlan", "Subscription plan deleted successfully", id);
                return ApplicationResponse.success("Subscription plan deleted successfully");
            } else {
                ApiLogger.error("SubscriptionPlanService.deleteSubscriptionPlan", "Subscription plan not found", id);
                return ApplicationResponse.error(ApiCode.SUBSCRIPTION_NOT_FOUND.getCode(), 
                        "Subscription plan not found with ID: " + id, 
                        ApiCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus());
            }
        } catch (Exception e) {
            ApiLogger.error("SubscriptionPlanService.deleteSubscriptionPlan", "Failed to delete subscription plan", e, id);
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), 
                    "Failed to delete subscription plan: " + e.getMessage(), 
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }
}
