package com.imovel.api.config.base;


import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.Configuration;
import com.imovel.api.model.Permissions;
import com.imovel.api.model.SubscriptionPlan;
import com.imovel.api.repository.ConfigurationRepository;
import com.imovel.api.repository.SubscriptionPlanRepository;
import com.imovel.api.repository.PermissionsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StartupConfig {

    private final ConfigurationRepository configurationRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PermissionsRepository permissionRepository;
    private final StartupConfigLoader appConfig;

    @Autowired
    public StartupConfig(ConfigurationRepository configurationRepository,
                         SubscriptionPlanRepository subscriptionPlanRepository,
                         PermissionsRepository permissionRepository,
                         StartupConfigLoader appConfig) {
        this.configurationRepository = configurationRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.permissionRepository = permissionRepository;
        this.appConfig = appConfig;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeApplicationData() {
        try {
            initializeDefaultConfigurations();
            initializeDefaultSubscriptionPlans();
            initializeDefaultPermissions();
            ApiLogger.info("Application data initialized successfully");
        } catch (Exception e) {
            ApiLogger.error("Failed to initialize application data", e);
        }
    }

    private void initializeDefaultConfigurations() {
        Map<String, String> configMap = appConfig.getDefaultConfig();
        if (configMap != null && !configMap.isEmpty()) {
            configMap.forEach(this::createConfigIfNotExists);
        } else {
            ApiLogger.warn("No default configurations found in config file");
        }
    }

    private void createConfigIfNotExists(String key, String value) {
        if (!configurationRepository.existsByConfigKey(key)) {
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
            List<PlanConfig> planConfigs = appConfig.getSubscriptionPlanConfig().getPlans();
            if (planConfigs != null && !planConfigs.isEmpty()) {
                for (PlanConfig planConfig : planConfigs) {
                    createSubscriptionPlan(planConfig);
                }
                ApiLogger.info("Default subscription plans initialized successfully from config");
            } else {
                ApiLogger.warn("No subscription plan configurations found in config file");
            }
        } catch (Exception e) {
            ApiLogger.error("Failed to initialize subscription plans from config", e);
        }
    }

    private void createSubscriptionPlan(PlanConfig planConfig) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(planConfig.getName());
        plan.setDescription(planConfig.getDescription());
        plan.setMonthlyPrice(planConfig.getMonthlyPrice());
        plan.setYearlyPrice(planConfig.getYearlyPrice());
        plan.setListingLimit(planConfig.getListingLimit());
        plan.setAvailabilityDays(planConfig.getAvailabilityDays());
        plan.setFeatured(planConfig.getFeatured() != null ? planConfig.getFeatured() : false);
        plan.setSupportType(planConfig.getSupportType());

        subscriptionPlanRepository.save(plan);
        ApiLogger.debug("Created subscription plan: {}", plan.getName());
    }

    private void initializeDefaultPermissions() {
        if (!permissionRepository.findAll().isEmpty()) {
            ApiLogger.info("Permissions already exist, skipping initialization");
            return;
        }
        try {
            List<StartupConfigLoader.PermissionEntry> permissionEntries =
                    appConfig.getPermissionConfig().getPermissions();

            if (permissionEntries != null && !permissionEntries.isEmpty()) {
                for (StartupConfigLoader.PermissionEntry permissionEntry : permissionEntries) {
                    createPermissionIfNotExists(permissionEntry.getName(), permissionEntry.getDescription());
                }
                ApiLogger.info("Default permissions initialized successfully from config");
            }
        } catch (Exception e) {
            ApiLogger.error("Failed to initialize permissions from config, falling back to hardcoded", e);
        }
    }

    private void createPermissionIfNotExists(String name, String description) {
        if (!permissionRepository.existsByPermissionName(name)) {
            Permissions permission = new Permissions();
            permission.setPermissionName(name);
            permission.setDescription(description);
            permissionRepository.save(permission);
            ApiLogger.debug("Created permission: {}", name);
        } else {
            ApiLogger.debug("Permission already exists: {}", name);
        }
    }
}