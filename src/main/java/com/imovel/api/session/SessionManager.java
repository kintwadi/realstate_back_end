package com.imovel.api.session;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.User;
import com.imovel.api.payment.dto.PaymentRefundRequest;
import com.imovel.api.payment.dto.PaymentRequest;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.services.TokenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class SessionManager {
    
    private final TokenService tokenService;
    private final  UserRepository userRepository;
    
    // Constructor injection (recommended)
    public SessionManager(TokenService tokenService,UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }
    
    public CurrentUser getCurrentUser(HttpSession session) {
        String token = (String) session.getAttribute("token");
        
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("No token found in session");
        }
        
        String id = tokenService.getClaim("userId", token);
        String userName = tokenService.getClaim("username", token);
        String role = tokenService.getClaim("role", token);
        
        if (id == null || userName == null) {
            throw new IllegalStateException("Invalid token: missing required claims");
        }
        
        return new CurrentUser(Long.parseLong(id), userName,role);
    }

    public User getCurrentAuthenticatedUser(HttpSession session) {

        return userRepository.findById(getCurrentUser(session).getUserId()).get();
    }


    public ResponseEntity<?> verifyAuthentication(HttpSession session, Long requestedUserId) {
        try {
            ApiLogger.info("🔐 Verifying authentication for user: " + requestedUserId);

            // Check if session exists
            if (session == null) {
                ApiLogger.warn("🚫 No HTTP session found");
                return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                        "Authentication required: No active session",
                        HttpStatus.UNAUTHORIZED);
            }

            ApiLogger.info("🔑 Session ID: " + session.getId());

            // Debug session contents
            try {
                java.util.Enumeration<String> attributeNames = session.getAttributeNames();
                ApiLogger.info("📦 Session attributes:");
                boolean hasAttributes = false;
                while (attributeNames.hasMoreElements()) {
                    hasAttributes = true;
                    String attrName = attributeNames.nextElement();
                    Object attrValue = session.getAttribute(attrName);
                    ApiLogger.info("   - " + attrName + ": " +
                            (attrValue != null ? attrValue.getClass().getSimpleName() : "null"));
                }
                if (!hasAttributes) {
                    ApiLogger.info("   (no attributes found)");
                }
            } catch (Exception e) {
                ApiLogger.error("❌ Error reading session attributes: " + e.getMessage());
            }

            // Get current user from session
            ApiLogger.info("👤 Getting current user from session...");
            CurrentUser currentUser;
            try {
                currentUser = getCurrentUser(session);
                ApiLogger.info("👤 Current user: " + (currentUser != null ?
                        "User[id=" + currentUser.getUserId() + ", username=" + currentUser.getUserName() + "]" : "null"));
            } catch (Exception e) {
                ApiLogger.error("💥 ERROR getting current user: " + e.getMessage(), e);
                return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                        "Authentication error: " + e.getMessage(),
                        HttpStatus.UNAUTHORIZED);
            }

            if (currentUser == null) {
                ApiLogger.warn("🚫 No authenticated user found in session");
                return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                        "Authentication required: Please login again",
                        HttpStatus.UNAUTHORIZED);
            }

            // Validate that the authenticated user matches the requested user
            if (!currentUser.getUserId().equals(requestedUserId)) {
                ApiLogger.warn("🚫 Access denied: Authenticated user " + currentUser.getUserId() +
                        " trying to access data for user " + requestedUserId);
                return createErrorResponse(ApiCode.ACCESS_DENIED.getCode(),
                        "Access denied: You can only access your own payment data",
                        HttpStatus.FORBIDDEN);
            }

            ApiLogger.info("✅ Authentication verified successfully for user: " + currentUser.getUserId());
            return null; // Return null if authentication is successful

        } catch (Exception e) {
            ApiLogger.error("💥 Unexpected error during authentication: " + e.getMessage(), e);
            return createErrorResponse(ApiCode.SYSTEM_ERROR.getCode(),
                    "Authentication validation failed",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extract user ID from different parameter types
     */
    private Long extractUserIdFromParameters(Object... parameters) {
        for (Object param : parameters) {
            // Check for direct Long userId
            if (param instanceof Long) {
                ApiLogger.info("🎯 Found user ID in Long parameter: " + param);
                return (Long) param;
            }
            // Check for PaymentRequest
            if (param instanceof PaymentRequest) {
                Long userId = ((PaymentRequest) param).getUserId();
                ApiLogger.info("🎯 Found user ID in PaymentRequest: " + userId);
                return userId;
            }
            // Check for PaymentRefundRequest
            if (param instanceof PaymentRefundRequest) {
                Long userId = ((PaymentRefundRequest) param).getUserId();
                ApiLogger.info("🎯 Found user ID in PaymentRefundRequest: " + userId);
                return userId;
            }
        }
        ApiLogger.warn("🔍 No user ID found in parameters");
        return null;
    }

    /**
     * Create error response
     */
    private ResponseEntity<ApplicationResponse<?>> createErrorResponse(int code, String message, HttpStatus status) {
        ErrorCode errorCode = new ErrorCode(code, message, status);
        ApplicationResponse<?> response = ApplicationResponse.error(errorCode);
        ApiLogger.info("📤 Returning error response: " + status.value() + " - " + message);
        return ResponseEntity.status(status).body(response);
    }
}
