package com.imovel.api.request;

import org.springframework.web.bind.annotation.RequestParam;

public class SubscriptionPlainRequest {

    private Long userId;
    private Long planId;
    private String billingCycle;

   public SubscriptionPlainRequest(){

   }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }
}
