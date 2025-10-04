package com.imovel.api.controller;


import com.imovel.api.request.ChangePlainRequest;
import com.imovel.api.request.SubscriptionPlainRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.SubscriptionResponse;
import com.imovel.api.response.UserSubscriptionResponse;
import com.imovel.api.services.SubscriptionService;
import com.imovel.api.session.SessionManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final SessionManager sessionManager;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService,SessionManager sessionManager) {
        this.subscriptionService = subscriptionService;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/subscribe")
    public ApplicationResponse<SubscriptionResponse> subscribe(@RequestBody SubscriptionPlainRequest subscriptionPlanRequest, HttpSession session) {

        // Authentication check
        ResponseEntity<?> authResponse = sessionManager.verifyAuthentication(session, subscriptionPlanRequest.getUserId());
        if (authResponse != null) {
            return (ApplicationResponse<SubscriptionResponse>)authResponse.getBody();
        }
        return subscriptionService.subscribeUser(subscriptionPlanRequest.getUserId(),
                                                 subscriptionPlanRequest.getPlanId(),
                                                 subscriptionPlanRequest.getBillingCycle());
    }

    @GetMapping("/user/{userId}")
    public ApplicationResponse<List<SubscriptionResponse>> getUserSubscriptions(@PathVariable Long userId,
                                                                                HttpSession session) {

        // Authentication check
        ResponseEntity<?> authResponse = sessionManager.verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ApplicationResponse<List<SubscriptionResponse>>)authResponse.getBody();
        }
        return subscriptionService.getUserSubscriptions(userId);
    }

    @GetMapping("/user/{userId}/details")
    public ApplicationResponse<UserSubscriptionResponse> getUserSubscriptionDetails(@PathVariable Long userId,
                                                                                    HttpSession session) {

        // Authentication check
        ResponseEntity<?> authResponse = sessionManager.verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ApplicationResponse<UserSubscriptionResponse>)authResponse.getBody();
        }
        return subscriptionService.getUserSubscriptionDetails(userId);
    }

    @PostMapping("cancel/{subscriptionId}/{userId}")
    public ApplicationResponse<SubscriptionResponse> cancelSubscription(@PathVariable Long subscriptionId,
                                                                        @PathVariable Long userId,
                                                                        HttpSession session) {

        // Authentication check
        ResponseEntity<?> authResponse = sessionManager.verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ApplicationResponse<SubscriptionResponse>)authResponse.getBody();
        }
        return subscriptionService.cancelSubscription(subscriptionId);
    }

    @PostMapping("change-plan")
    public ApplicationResponse<SubscriptionResponse> changePlan(@RequestBody ChangePlainRequest changePlainRequest,
                                                                HttpSession session) {

        // Authentication check
        ResponseEntity<?> authResponse = sessionManager.verifyAuthentication(session, changePlainRequest.getUserId());
        if (authResponse != null) {
            return (ApplicationResponse<SubscriptionResponse>)authResponse.getBody();
        }
        return subscriptionService.changePlan(changePlainRequest.getSubscriptionId(),
                                              changePlainRequest.getNewPlanId(),
                                              changePlainRequest.isMediate());
    }

    @PostMapping("restore/{subscriptionId}/{userId}")
    public ApplicationResponse<SubscriptionResponse> restoreSubscription(@PathVariable Long subscriptionId,
                                                                        @PathVariable Long userId,
                                                                        HttpSession session) {

        // Authentication check
        ResponseEntity<?> authResponse = sessionManager.verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ApplicationResponse<SubscriptionResponse>)authResponse.getBody();
        }
        return subscriptionService.restoreSubscription(subscriptionId);
    }
}