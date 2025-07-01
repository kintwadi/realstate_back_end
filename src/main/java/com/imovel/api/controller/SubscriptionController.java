package com.imovel.api.controller;

import com.imovel.api.model.Subscription;
import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<StandardResponse<List<SubscriptionPlan>>> getAllPlans() {
        //subscriptionService.init();
        StandardResponse<List<SubscriptionPlan>> response = subscriptionService.getAllPlans();
        return ResponseEntity.status(response.getError() != null ? 
                response.getError().getStatus() : HttpStatus.OK).body(response);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<StandardResponse<Subscription>> subscribe(
            @RequestParam Long userId,
            @RequestParam Long planId,
            @RequestParam String billingCycle) {
        StandardResponse<Subscription> response = subscriptionService.subscribeUser(userId, planId, billingCycle);
        return ResponseEntity.status(response.getError() != null ? 
                response.getError().getStatus() : HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StandardResponse<List<Subscription>>> getUserSubscriptions(@PathVariable Long userId) {
        StandardResponse<List<Subscription>> response = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.status(response.getError() != null ? 
                response.getError().getStatus() : HttpStatus.OK).body(response);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<StandardResponse<Subscription>> cancelSubscription(@PathVariable Long subscriptionId) {
        StandardResponse<Subscription> response = subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.status(response.getError() != null ? 
                response.getError().getStatus() : HttpStatus.OK).body(response);
    }
    @PostMapping("/{subscriptionId}/change-plan")
    public ResponseEntity<StandardResponse<Subscription>> changePlan(
            @PathVariable Long subscriptionId,
            @RequestParam Long newPlanId,
            @RequestParam(required = false, defaultValue = "true") boolean immediate) {

        StandardResponse<Subscription> response = subscriptionService.changePlan(
                subscriptionId, newPlanId, immediate);

        return ResponseEntity.status(response.getError() != null ?
                response.getError().getStatus() : HttpStatus.OK).body(response);
    }
}