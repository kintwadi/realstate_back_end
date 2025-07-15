package com.imovel.api.response;

import com.imovel.api.model.Subscription;
import com.imovel.api.model.SubscriptionPlan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SubscriptionResponse {
    private Long id;
    private SubscriptionPlanResponse plan;
    private Long userId;
    private String billingCycle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubscriptionPlanResponse getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlanResponse plan) {
        this.plan = plan;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Parse methods
    public static SubscriptionResponse parse(Subscription subscription) {
        if (subscription == null) {
            return null;
        }

        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setPlan(SubscriptionPlanResponse.parse(subscription.getPlan()));
        response.setUserId(subscription.getUserId());
        response.setBillingCycle(subscription.getBillingCycle());
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setStatus(subscription.getStatus());
        
        return response;
    }

    public static List<SubscriptionResponse> parse(List<Subscription> subscriptions) {
        if (subscriptions == null) {
            return null;
        }

        return subscriptions.stream()
                .map(SubscriptionResponse::parse)
                .collect(Collectors.toList());
    }
}