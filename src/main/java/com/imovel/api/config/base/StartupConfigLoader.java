package com.imovel.api.config.base;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Configuration;
@Component
@Configuration
@ConfigurationProperties(prefix = "startup-config")
public class StartupConfigLoader {

    private Map<String, String> defaultConfig = new HashMap<>();
    private SubscriptionPlanConfig subscriptionPlanConfig = new SubscriptionPlanConfig();
    private PermissionConfig permissionConfig = new PermissionConfig();

    // Getters and setters
    public Map<String, String> getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(Map<String, String> defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public SubscriptionPlanConfig getSubscriptionPlanConfig() {
        return subscriptionPlanConfig;
    }

    public void setSubscriptionPlanConfig(SubscriptionPlanConfig subscriptionPlanConfig) {
        this.subscriptionPlanConfig = subscriptionPlanConfig;
    }

    public PermissionConfig getPermissionConfig() {
        return permissionConfig;
    }

    public void setPermissionConfig(PermissionConfig permissionConfig) {
        this.permissionConfig = permissionConfig;
    }

    public static class SubscriptionPlanConfig {
        private List<PlanConfig> plans = new ArrayList<>();

        public List<PlanConfig> getPlans() {
            return plans;
        }
        public void setPlans(List<PlanConfig> plans) {
            this.plans = plans;
        }
    }

    public static class PermissionConfig {
        private List<PermissionEntry> permissions = new ArrayList<>();

        public List<PermissionEntry> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<PermissionEntry> permissions) {
            this.permissions = permissions;
        }
    }

    public static class PermissionEntry {
        private String name;
        private String description;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}