package com.imovel.api.util;

import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.repository.SubscriptionPlanRepository;

import java.math.BigDecimal;


public class SubscriptionPlanInitializer {

    public static void initializeDefaultSubscriptionPlan(SubscriptionPlanRepository repository) {

        if(!repository.findAllByOrderByIdAsc().isEmpty())return;
        // Free Plan
        SubscriptionPlan free = new SubscriptionPlan();
        free.setName("Free");
        free.setDescription("Basic listing with limited features");
        free.setMonthlyPrice(BigDecimal.ZERO);
        free.setYearlyPrice(BigDecimal.ZERO);
        free.setListingLimit(1);
        free.setAvailabilityDays(90);
        free.setFeatured(false);
        free.setSupportType("limited");
        repository.save(free);

        // Basic Plan
        SubscriptionPlan basic = new SubscriptionPlan();
        basic.setName("Basic");
        basic.setDescription("More listings with extended availability");
        basic.setMonthlyPrice(new BigDecimal("49"));
        basic.setYearlyPrice(new BigDecimal("530"));
        basic.setListingLimit(20);
        basic.setAvailabilityDays(190);
        basic.setFeatured(false);
        basic.setSupportType("limited");
        repository.save(basic);

        // Extended Plan
        SubscriptionPlan extended = new SubscriptionPlan();
        extended.setName("Extended");
        extended.setDescription("Unlimited listings with longer availability");
        extended.setMonthlyPrice(new BigDecimal("109"));
        extended.setYearlyPrice(new BigDecimal("1100"));
        extended.setListingLimit(null); // unlimited
        extended.setAvailabilityDays(220);
        extended.setFeatured(false);
        extended.setSupportType("limited");
        repository.save(extended);

        // Premium Plan
        SubscriptionPlan premium = new SubscriptionPlan();
        premium.setName("Premium");
        premium.setDescription("Full features with priority support");
        premium.setMonthlyPrice(new BigDecimal("149"));
        premium.setYearlyPrice(new BigDecimal("1430"));
        premium.setListingLimit(null); // unlimited
        premium.setAvailabilityDays(null); // lifetime
        premium.setFeatured(true);
        premium.setSupportType("24/7");
        repository.save(premium);
    }


}