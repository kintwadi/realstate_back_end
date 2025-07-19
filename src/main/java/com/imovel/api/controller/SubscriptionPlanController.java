package com.imovel.api.controller;

import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.request.SubscriptionPlainRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.SubscriptionPlanResponse;
import com.imovel.api.services.SubscriptionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @Autowired
    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @PostMapping
    public ApplicationResponse<SubscriptionPlanResponse> createSubscriptionPlan(@RequestBody SubscriptionPlan subscriptionPlan) {
        return  subscriptionPlanService.createSubscriptionPlan(subscriptionPlan);
    }

    @GetMapping("/{id}")
    public ApplicationResponse<SubscriptionPlanResponse> getSubscriptionPlanById(@PathVariable Long id) {
        return subscriptionPlanService.getSubscriptionPlanById(id);
    }

    @GetMapping
    public ApplicationResponse<List<SubscriptionPlanResponse>> getAllSubscriptionPlans() {
        return  subscriptionPlanService.getAllSubscriptionPlans();
    }

    @PutMapping("/{id}")
    public ApplicationResponse<SubscriptionPlanResponse> updateSubscriptionPlan(
            @PathVariable Long id, 
            @RequestBody SubscriptionPlan updatedPlan) {
        return  subscriptionPlanService.updateSubscriptionPlan(id, updatedPlan);
    }

    @DeleteMapping("/{id}")
    public ApplicationResponse<Void> deleteSubscriptionPlan(@PathVariable Long id) {
        return subscriptionPlanService.deleteSubscriptionPlan(id);
    }
}