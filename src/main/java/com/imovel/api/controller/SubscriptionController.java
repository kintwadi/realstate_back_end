package com.imovel.api.controller;

import com.imovel.api.model.Subscription;
import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/plans")
    public ApplicationResponse<List<SubscriptionPlan>> getAllPlans() {
        return subscriptionService.getAllPlans();
    }

    @PostMapping("/subscribe")
    public ApplicationResponse<Subscription> subscribe(
            @RequestParam Long userId,
            @RequestParam Long planId,
            @RequestParam String billingCycle) {
        return subscriptionService.subscribeUser(userId, planId, billingCycle);
    }

    @GetMapping("/user/{userId}")
    public ApplicationResponse<List<Subscription>> getUserSubscriptions(@PathVariable Long userId) {
        return subscriptionService.getUserSubscriptions(userId);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ApplicationResponse<Subscription> cancelSubscription(@PathVariable Long subscriptionId) {
        return subscriptionService.cancelSubscription(subscriptionId);
    }

    @PostMapping("/{subscriptionId}/change-plan")
    public ApplicationResponse<Subscription> changePlan(
            @PathVariable Long subscriptionId,
            @RequestParam Long newPlanId,
            @RequestParam(required = false, defaultValue = "true") boolean immediate) {
        return subscriptionService.changePlan(subscriptionId, newPlanId, immediate);
    }
}