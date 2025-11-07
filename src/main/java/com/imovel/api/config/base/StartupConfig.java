package com.imovel.api.config.base;

import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.Configuration;
import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.repository.ConfigurationRepository;
import com.imovel.api.repository.SubscriptionPlanRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;


@Component
public class StartupConfig {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    // Configuration constants
    public static final String ACCESS_EXPIRATION_KEY = "ACCESS_EXPIRATION_MS";
    public static final String REFRESH_EXPIRATION_KEY = "REFRESH_EXPIRATION_MS";
    private static final String DEFAULT_ACCESS_TOKEN_EXPIRATION_VALUE = "900000";
    private static final String DEFAULT_REFRESH_EXPIRATION_VALUE = "604800000";
    public static final String MAX_REFRESH_TOKEN_PER_USER_KEY = "MAX_REFRESH_TOKEN_PER_USER";
    private static final String MAX_REFRESH_TOKEN_PER_USER_VALUE = "5";
    public static final String REFRESH_CLEAN_UP_INTERVAL_KEY = "REFRESH_CLEAN_UP_INTERVAL";
    private static final String REFRESH_CLEAN_UP_INTERVAL_VALUE = "86400000";

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeApplicationData() {
        try {
            initializeDefaultConfigurations();
            initializeDefaultSubscriptionPlans();
            ApiLogger.info("Application data initialized successfully");
        } catch (Exception e) {
            ApiLogger.error("Failed to initialize application data", e);
        }
    }

    private void initializeDefaultConfigurations() {
        Map<String, String> defaultConfigs = Map.of(
                ACCESS_EXPIRATION_KEY, DEFAULT_ACCESS_TOKEN_EXPIRATION_VALUE,
                REFRESH_EXPIRATION_KEY, DEFAULT_REFRESH_EXPIRATION_VALUE,
                MAX_REFRESH_TOKEN_PER_USER_KEY, MAX_REFRESH_TOKEN_PER_USER_VALUE,
                REFRESH_CLEAN_UP_INTERVAL_KEY, REFRESH_CLEAN_UP_INTERVAL_VALUE
        );

        defaultConfigs.forEach(this::createConfigIfNotExists);
    }

    private void createConfigIfNotExists(String key, String value) {
        if (!configurationRepository.existsByKey(key)) {
            Configuration config = new Configuration(key, value);
            configurationRepository.save(config);
            ApiLogger.info("Created default configuration: {} = {}", key, value);
        } else {
            ApiLogger.debug("Configuration already exists: {}", key);
        }
    }

    private void initializeDefaultSubscriptionPlans() {
        if (!subscriptionPlanRepository.findAllByOrderByIdAsc().isEmpty()) {
            ApiLogger.info("Subscription plans already exist, skipping initialization");
            return;
        }

        try {
            createFreePlan();
            createBasicPlan();
            createExtendedPlan();
            createPremiumPlan();
            ApiLogger.info("Default subscription plans initialized successfully");
        } catch (Exception e) {
            ApiLogger.error("Failed to initialize subscription plans", e);
        }
    }

    private void createFreePlan() {
        SubscriptionPlan free = new SubscriptionPlan();
        free.setName("Free");
        free.setDescription("Basic listing with limited features");
        free.setMonthlyPrice(BigDecimal.ZERO);
        free.setYearlyPrice(BigDecimal.ZERO);
        free.setListingLimit(1);
        free.setAvailabilityDays(90);
        free.setFeatured(false);
        free.setSupportType("limited");
        subscriptionPlanRepository.save(free);
        ApiLogger.debug("Created Free subscription plan");
    }

    private void createBasicPlan() {
        SubscriptionPlan basic = new SubscriptionPlan();
        basic.setName("Basic");
        basic.setDescription("More listings with extended availability");
        basic.setMonthlyPrice(new BigDecimal("49"));
        basic.setYearlyPrice(new BigDecimal("530"));
        basic.setListingLimit(20);
        basic.setAvailabilityDays(190);
        basic.setFeatured(false);
        basic.setSupportType("limited");
        subscriptionPlanRepository.save(basic);
        ApiLogger.debug("Created Basic subscription plan");
    }

    private void createExtendedPlan() {
        SubscriptionPlan extended = new SubscriptionPlan();
        extended.setName("Extended");
        extended.setDescription("Unlimited listings with longer availability");
        extended.setMonthlyPrice(new BigDecimal("109"));
        extended.setYearlyPrice(new BigDecimal("1100"));
        extended.setListingLimit(null); // unlimited
        extended.setAvailabilityDays(220);
        extended.setFeatured(false);
        extended.setSupportType("limited");
        subscriptionPlanRepository.save(extended);
        ApiLogger.debug("Created Extended subscription plan");
    }

    private void createPremiumPlan() {
        SubscriptionPlan premium = new SubscriptionPlan();
        premium.setName("Premium");
        premium.setDescription("Full features with priority support");
        premium.setMonthlyPrice(new BigDecimal("149"));
        premium.setYearlyPrice(new BigDecimal("1430"));
        premium.setListingLimit(null); // unlimited
        premium.setAvailabilityDays(null); // lifetime
        premium.setFeatured(true);
        premium.setSupportType("24/7");
        subscriptionPlanRepository.save(premium);
        ApiLogger.debug("Created Premium subscription plan");
    }

    // Static method for external use
    public static void initializeDefaultSubscriptionPlan(SubscriptionPlanRepository repository) {
        if (!repository.findAllByOrderByIdAsc().isEmpty()) {
            return;
        }

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