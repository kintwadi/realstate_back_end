package com.imovel.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

public class Config {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Specify allowed origins (Angular dev server, production frontend URL)
        // Use "*" cautiously for development only, be specific in production
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:4200", "http://pot-frontend.s3-website.eu-north-1.amazonaws.com" ));
        // Specify allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // Crucial: Include OPTIONS
        // Specify allowed headers
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "Accept", "auth-type")); // Include headers your frontend might send
        // configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition")); // If frontend needs to read specific headers
        // * Crucial for cookie-based authentication (like HttpOnly JWT) *
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Optional: Cache preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/*", configuration); // Apply this config to all paths ("/*")
        return source;
    }
}


