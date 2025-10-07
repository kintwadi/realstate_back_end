package com.imovel.api.payment.audit;

import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Utility class for managing audit context and request tracking.
 * Provides methods to capture and store request context for audit logging.
 */
public final class AuditContext {
    
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String USER_ID_KEY = "userId";
    private static final String IP_ADDRESS_KEY = "ipAddress";
    private static final String USER_AGENT_KEY = "userAgent";
    
    private AuditContext() {}
    
    /**
     * Initialize audit context for a new request
     */
    public static void initializeContext() {
        String requestId = generateRequestId();
        MDC.put(REQUEST_ID_KEY, requestId);
        
        // Capture request details if available
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Set session ID
                if (request.getSession(false) != null) {
                    MDC.put(SESSION_ID_KEY, request.getSession().getId());
                }
                
                // Set IP address
                String ipAddress = getClientIpAddress(request);
                MDC.put(IP_ADDRESS_KEY, ipAddress);
                
                // Set user agent
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    MDC.put(USER_AGENT_KEY, userAgent);
                }
                
                // Set correlation ID from header if present
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId == null) {
                    correlationId = generateCorrelationId();
                }
                MDC.put(CORRELATION_ID_KEY, correlationId);
            }
        } catch (Exception e) {
            // Ignore errors in context initialization
        }
    }
    
    /**
     * Set user ID in audit context
     */
    public static void setUserId(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId.toString());
        }
    }
    
    /**
     * Set correlation ID in audit context
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
    }
    
    /**
     * Get current request ID
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }
    
    /**
     * Get current session ID
     */
    public static String getSessionId() {
        return MDC.get(SESSION_ID_KEY);
    }
    
    /**
     * Get current correlation ID
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }
    
    /**
     * Get current user ID
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }
    
    /**
     * Get current IP address
     */
    public static String getIpAddress() {
        return MDC.get(IP_ADDRESS_KEY);
    }
    
    /**
     * Get current user agent
     */
    public static String getUserAgent() {
        return MDC.get(USER_AGENT_KEY);
    }
    
    /**
     * Clear audit context
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * Generate a unique request ID
     */
    private static String generateRequestId() {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Generate a unique correlation ID
     */
    private static String generateCorrelationId() {
        return "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Extract client IP address from request, considering proxy headers
     */
    private static String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated IPs (X-Forwarded-For can contain multiple IPs)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
}
