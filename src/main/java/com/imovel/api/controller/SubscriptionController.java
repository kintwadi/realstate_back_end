package com.imovel.api.controller;


import com.imovel.api.request.SubscriptionPlainRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.SubscriptionPlanResponse;
import com.imovel.api.response.SubscriptionResponse;
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

    @PostMapping("/subscribe")
    public ApplicationResponse<SubscriptionResponse> subscribe(@RequestBody SubscriptionPlainRequest subscriptionPlanRequest) {

        return subscriptionService.subscribeUser(subscriptionPlanRequest.getUserId(),
                                                 subscriptionPlanRequest.getPlanId(),
                                                 subscriptionPlanRequest.getBillingCycle());
    }

    @GetMapping("/user/{userId}")
    public ApplicationResponse<List<SubscriptionResponse>> getUserSubscriptions(@PathVariable Long userId) {
        return subscriptionService.getUserSubscriptions(userId);
    }
    // NOT TESTED
    @PostMapping("/{subscriptionId}/cancel")
    public ApplicationResponse<SubscriptionResponse> cancelSubscription(@PathVariable Long subscriptionId) {
        return subscriptionService.cancelSubscription(subscriptionId);
    }

    @PostMapping("/{subscriptionId}/change-plan")
    public ApplicationResponse<SubscriptionResponse> changePlan(
            @PathVariable Long subscriptionId,
            @RequestParam Long newPlanId,
            @RequestParam(required = false, defaultValue = "true") boolean immediate) {
        return subscriptionService.changePlan(subscriptionId, newPlanId, immediate);
    }
}