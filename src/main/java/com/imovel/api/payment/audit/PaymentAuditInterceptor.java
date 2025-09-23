package com.imovel.api.payment.audit;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor for payment audit logging.
 * Automatically initializes audit context for payment-related requests.
 */
@Component
public class PaymentAuditInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Initialize audit context for payment requests
        if (isPaymentRequest(request)) {
            AuditContext.initializeContext();
            
            // Log request initiation
            PaymentAuditLogger.logSecurityEvent(
                "REQUEST_INITIATED",
                "Payment API request initiated: " + request.getMethod() + " " + request.getRequestURI(),
                AuditContext.getIpAddress(),
                AuditContext.getUserAgent(),
                null
            );
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        // Log request completion and clear context
        if (isPaymentRequest(request)) {
            PaymentAuditLogger.logSecurityEvent(
                "REQUEST_COMPLETED",
                "Payment API request completed: " + response.getStatus(),
                AuditContext.getIpAddress(),
                AuditContext.getUserAgent(),
                null
            );
            
            AuditContext.clearContext();
        }
    }
    
    /**
     * Check if the request is payment-related
     */
    private boolean isPaymentRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/api/payments") || 
               uri.contains("/api/webhooks") ||
               uri.contains("/stripe");
    }
}