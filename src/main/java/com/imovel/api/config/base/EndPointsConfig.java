package com.imovel.api.config.base;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "server.security")
public class EndPointsConfig {
    

    private List<String> protectedEndpoints = new ArrayList<>();

    private List<String> excludedEndpoints = new ArrayList<>();
    
    // Default constructor
    public EndPointsConfig() {
    }
    // Getters and Setters
    public List<String> getProtectedEndpoints() {
        return protectedEndpoints;
    }

    public void setProtectedEndpoints(List<String> protectedEndpoints) {
        this.protectedEndpoints = protectedEndpoints;
    }

    public List<String> getExcludedEndpoints() {
        return excludedEndpoints;
    }

    public void setExcludedEndpoints(List<String> excludedEndpoints) {
        this.excludedEndpoints = excludedEndpoints;
    }

}