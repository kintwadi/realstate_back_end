package com.imovel.api.payment.controller;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.payment.dto.PaymentRefundRequest;
import com.imovel.api.payment.dto.PaymentRequest;
import com.imovel.api.payment.dto.PaymentResponse;
import com.imovel.api.payment.monitoring.PaymentMonitoringService;
import com.imovel.api.payment.service.PaymentService;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.CurrentUser;
import com.imovel.api.session.SessionManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.imovel.api.pagination.Pagination;
import com.imovel.api.pagination.PaginationResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMonitoringService monitoringService;
    private final SessionManager sessionManager;

    @Autowired
    public PaymentController(PaymentService paymentService, PaymentMonitoringService monitoringService,
                             SessionManager sessionManager) {
        this.paymentService = paymentService;
        this.monitoringService = monitoringService;
        this.sessionManager = sessionManager;
    }


    private ResponseEntity<?> verifyAuthentication(HttpSession session, Long requestedUserId) {
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
                currentUser = sessionManager.getCurrentUser(session);
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

    /**
     * Process a new payment
     */
    @PostMapping("/process")
    @RateLimiter(name = "paymentProcessing", fallbackMethod = "paymentRateLimitFallback")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> processPayment(
            @RequestBody PaymentRequest paymentRequest, HttpSession session) {

        // Authentication check - added this line
        ResponseEntity<?> authResponse = verifyAuthentication(session, paymentRequest.getUserId());
        if (authResponse != null) {
            return (ResponseEntity<ApplicationResponse<PaymentResponse>>) authResponse;
        }

        ApiLogger.info("Processing payment request for user: " + paymentRequest.getUserId());

        ApplicationResponse<PaymentResponse> response = paymentService.processPayment(paymentRequest, paymentRequest.getUserId());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long paymentId,
            @RequestParam Long userId, HttpSession session) {

        // Authentication check - added this line
        ResponseEntity<?> authResponse = verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ResponseEntity<ApplicationResponse<PaymentResponse>>) authResponse;
        }

        ApiLogger.info("Retrieving payment by ID: " + paymentId + " for user: " + userId);

        ApplicationResponse<PaymentResponse> response = paymentService.getPaymentById(paymentId, userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }

    /**
     * Get user's payment history with pagination
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApplicationResponse<PaginationResult<PaymentResponse>>> getUserPayments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection, HttpSession session) {

        // Authentication check - added this line
        ResponseEntity<?> authResponse = verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ResponseEntity<ApplicationResponse<PaginationResult<PaymentResponse>>>) authResponse;
        }

        ApiLogger.info("Retrieving payments for user: " + userId);

        Pagination pagination = new Pagination();
        pagination.setPageNumber(page);
        pagination.setPageSize(size);

        ApplicationResponse<PaginationResult<PaymentResponse>> response = paymentService.getUserPayments(userId, pagination, sortBy, sortDirection);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }

    /**
     * Process a refund
     */
    @PostMapping("/refund")
    @RateLimiter(name = "paymentRefund", fallbackMethod = "refundRateLimitFallback")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> processRefund(@RequestBody PaymentRefundRequest paymentRefund, HttpSession session) {

        // Authentication check - added this line
        ResponseEntity<?> authResponse = verifyAuthentication(session, paymentRefund.getUserId());
        if (authResponse != null) {
            return (ResponseEntity<ApplicationResponse<PaymentResponse>>) authResponse;
        }

        // Assign variables from the request object
        Long paymentId = paymentRefund.getPaymentId();
        BigDecimal refundAmount = paymentRefund.getRefundAmount();
        String reason = paymentRefund.getReason();
        Long userId = paymentRefund.getUserId();

        ApiLogger.info("Processing refund for payment: " + paymentId + ", amount: " + refundAmount);

        ApplicationResponse<PaymentResponse> response = paymentService.processRefund(
                paymentId, refundAmount, reason, userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }

    /**
     * Cancel a payment
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam Long userId, HttpSession session) {

        // Authentication check - added this line
        ResponseEntity<?> authResponse = verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ResponseEntity<ApplicationResponse<PaymentResponse>>) authResponse;
        }

        ApiLogger.info("Cancelling payment: " + paymentId + " for user: " + userId);

        ApplicationResponse<PaymentResponse> response = paymentService.cancelPayment(paymentId, userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }

    /**
     * Verify payment status with gateway
     */
    @PostMapping("/{paymentId}/verify")
    @RateLimiter(name = "paymentVerification", fallbackMethod = "verificationRateLimitFallback")
    public ResponseEntity<ApplicationResponse<PaymentResponse>> verifyPaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam Long userId, HttpSession session) {

        // Authentication check - added this line
        ResponseEntity<?> authResponse = verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ResponseEntity<ApplicationResponse<PaymentResponse>>) authResponse;
        }

        ApiLogger.info("Verifying payment status for payment: " + paymentId + " for user: " + userId);

        ApplicationResponse<PaymentResponse> response = paymentService.verifyPaymentStatus(paymentId, userId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }

    /**
     * Get payment statistics for a user within a date range
     */
    @GetMapping("/statistics/{userId}")
    public ResponseEntity<ApplicationResponse<PaymentService.PaymentStatistics>> getPaymentStatistics(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, HttpSession session) {

        // Authentication check - added this line
        ResponseEntity<?> authResponse = verifyAuthentication(session, userId);
        if (authResponse != null) {
            return (ResponseEntity<ApplicationResponse<PaymentService.PaymentStatistics>>) authResponse;
        }

        ApiLogger.info("Retrieving payment statistics for user: " + userId);

        ApplicationResponse<PaymentService.PaymentStatistics> response =
                paymentService.getPaymentStatistics(userId, startDate, endDate);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getError().getStatus()).body(response);
        }
    }

    // ... existing webhook and fallback methods remain unchanged ...

    /**
     * DEBUG ENDPOINT: Test session and authentication
     */
    @GetMapping("/debug-session")
    public ResponseEntity<Map<String, Object>> debugSession(HttpSession session) {
        Map<String, Object> debugInfo = new HashMap<>();

        try {
            debugInfo.put("sessionId", session != null ? session.getId() : "null");
            debugInfo.put("sessionCreated", session != null ? new Date(session.getCreationTime()) : "null");

            if (session != null) {
                Map<String, String> attributes = new HashMap<>();
                java.util.Enumeration<String> attributeNames = session.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    String attrName = attributeNames.nextElement();
                    Object attrValue = session.getAttribute(attrName);
                    attributes.put(attrName, attrValue != null ? attrValue.getClass().getSimpleName() : "null");
                }
                debugInfo.put("sessionAttributes", attributes);
            }

            // Try to get current user
            try {
                CurrentUser currentUser = sessionManager.getCurrentUser(session);
                if (currentUser != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", currentUser.getUserId());
                    userInfo.put("username", currentUser.getUserName());
                    debugInfo.put("currentUser", userInfo);
                } else {
                    debugInfo.put("currentUser", "null");
                }
            } catch (Exception e) {
                debugInfo.put("currentUserError", e.getMessage());
            }

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            return ResponseEntity.status(500).body(debugInfo);
        }
    }
}
