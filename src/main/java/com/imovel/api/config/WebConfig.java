package com.imovel.api.config;

import com.imovel.api.payment.audit.PaymentAuditInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for registering interceptors and other web-related configurations.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register payment audit interceptor for all payment-related endpoints
        registry.addInterceptor(new PaymentAuditInterceptor())
                .addPathPatterns("/api/payments/**", "/api/webhooks/**");
    }
}
