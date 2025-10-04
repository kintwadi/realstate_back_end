package com.imovel.api.response;

import com.imovel.api.model.UserSubscription;
import com.imovel.api.model.SubscriptionPlan;
import java.time.LocalDateTime;

public class UserSubscriptionResponse {
    private Long id;
    private Long subscriptionId;
    private Long userId;
    private SubscriptionPlanResponse basePlan;
    private SubscriptionPlanResponse currentPlan;
    private boolean hasChangedPlan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public UserSubscriptionResponse() {}
    
    // Constructor from UserSubscription entity
    public UserSubscriptionResponse(UserSubscription userSubscription) {
        this.id = userSubscription.getId();
        this.subscriptionId = userSubscription.getSubscriptionId();
        this.userId = userSubscription.getUserId();
        this.basePlan = SubscriptionPlanResponse.parse(userSubscription.getBasePlan());
        this.currentPlan = SubscriptionPlanResponse.parse(userSubscription.getCurrentPlan());
        this.hasChangedPlan = userSubscription.hasChangedPlan();
        this.createdAt = userSubscription.getCreatedAt();
        this.updatedAt = userSubscription.getUpdatedAt();
    }
    
    // Static factory method
    public static UserSubscriptionResponse from(UserSubscription userSubscription) {
        return new UserSubscriptionResponse(userSubscription);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSubscriptionId() {
        return subscriptionId;
    }
    
    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public SubscriptionPlanResponse getBasePlan() {
        return basePlan;
    }
    
    public void setBasePlan(SubscriptionPlanResponse basePlan) {
        this.basePlan = basePlan;
    }
    
    public SubscriptionPlanResponse getCurrentPlan() {
        return currentPlan;
    }
    
    public void setCurrentPlan(SubscriptionPlanResponse currentPlan) {
        this.currentPlan = currentPlan;
    }
    
    public boolean isHasChangedPlan() {
        return hasChangedPlan;
    }
    
    public void setHasChangedPlan(boolean hasChangedPlan) {
        this.hasChangedPlan = hasChangedPlan;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "UserSubscriptionResponse{" +
                "id=" + id +
                ", subscriptionId=" + subscriptionId +
                ", userId=" + userId +
                ", basePlan=" + (basePlan != null ? basePlan.getName() : "null") +
                ", currentPlan=" + (currentPlan != null ? currentPlan.getName() : "null") +
                ", hasChangedPlan=" + hasChangedPlan +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}