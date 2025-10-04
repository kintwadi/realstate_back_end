package com.imovel.api.request;

public class ChangePlainRequest {

    private Long userId;
    private Long subscriptionId;
    private Long newPlanId;
    private String billingCycle;
    private String currency;
    private boolean mediate;

    public ChangePlainRequest(){

   }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNewPlanId() {
        return newPlanId;
    }

    public void setNewPlanId(Long newPlanId) {
        this.newPlanId = newPlanId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isMediate() {
        return mediate;
    }

    public void setMediate(boolean mediate) {
        this.mediate = mediate;
    }
}
