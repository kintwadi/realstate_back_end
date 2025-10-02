package com.imovel.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration class to ensure proper entity manager factory setup
 */
@Configuration
@EnableJpaRepositories(basePackages = {"com.imovel.api.repository", "com.imovel.api.payment.repository"})
@EntityScan(basePackages = {"com.imovel.api.model", "com.imovel.api.payment.model"})
@EnableTransactionManagement
public class JpaConfig {
    // Spring Boot auto-configuration will handle the rest
}