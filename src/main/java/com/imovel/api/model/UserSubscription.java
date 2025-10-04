package com.imovel.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "base_plan_id", nullable = false)
    private SubscriptionPlan basePlan;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_plan_id", nullable = false)
    private SubscriptionPlan currentPlan;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Default constructor
    public UserSubscription() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor for new subscription (basePlan = currentPlan initially)
    public UserSubscription(Long subscriptionId, Long userId, SubscriptionPlan plan) {
        this();
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.basePlan = plan;
        this.currentPlan = plan;
    }
    
    // Constructor for plan change
    public UserSubscription(Long subscriptionId, Long userId, SubscriptionPlan basePlan, SubscriptionPlan currentPlan) {
        this();
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.basePlan = basePlan;
        this.currentPlan = currentPlan;
    }
    
    // Method to update current plan (for plan changes)
    public void updateCurrentPlan(SubscriptionPlan newPlan) {
        this.currentPlan = newPlan;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Check if user has changed from their base plan
    public boolean hasChangedPlan() {
        return !this.basePlan.getId().equals(this.currentPlan.getId());
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
    
    public SubscriptionPlan getBasePlan() {
        return basePlan;
    }
    
    public void setBasePlan(SubscriptionPlan basePlan) {
        this.basePlan = basePlan;
    }
    
    public SubscriptionPlan getCurrentPlan() {
        return currentPlan;
    }
    
    public void setCurrentPlan(SubscriptionPlan currentPlan) {
        this.currentPlan = currentPlan;
        this.updatedAt = LocalDateTime.now();
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
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "UserSubscription{" +
                "id=" + id +
                ", subscriptionId=" + subscriptionId +
                ", userId=" + userId +
                ", basePlan=" + (basePlan != null ? basePlan.getName() : "null") +
                ", currentPlan=" + (currentPlan != null ? currentPlan.getName() : "null") +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}