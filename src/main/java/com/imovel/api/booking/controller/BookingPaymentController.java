package com.imovel.api.booking.controller;

import com.imovel.api.booking.request.BookingPaymentRequest;
import com.imovel.api.booking.response.BookingPaymentResponse;
import com.imovel.api.booking.service.BookingPaymentService;
import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.pagination.PaginationResult;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.CurrentUser;
import com.imovel.api.session.SessionManager;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for handling booking payment operations.
 * Provides endpoints for processing payments, refunds, and payment history.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/booking-payments")
public class BookingPaymentController {

    private final BookingPaymentService paymentService;
    private final SessionManager sessionManager;

    @Autowired
    public BookingPaymentController(BookingPaymentService paymentService, SessionManager sessionManager) {
        this.paymentService = paymentService;
        this.sessionManager = sessionManager;
    }

    private String buildLogTag(String method) {
        return "BookingPaymentController#" + method;
    }

    private ResponseEntity<ApplicationResponse<?>> verifyAuthentication(HttpSession session) {
        try {
            if (session == null) {
                ApiLogger.warn(buildLogTag("verifyAuthentication"), "No HTTP session found");
                return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                        "Authentication required: No active session",
                        HttpStatus.UNAUTHORIZED);
            }

            CurrentUser currentUser = sessionManager.getCurrentUser(session);
            if (currentUser == null) {
                ApiLogger.warn(buildLogTag("verifyAuthentication"), "No authenticated user found in session");
                return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                        "Authentication required: Please login again",
                        HttpStatus.UNAUTHORIZED);
            }

            return null; // Authentication successful
        } catch (Exception e) {
            ApiLogger.error(buildLogTag("verifyAuthentication"), "Authentication error: " + e.getMessage(), e);
            return createErrorResponse(ApiCode.INVALID_TOKEN.getCode(),
                    "Authentication error: " + e.getMessage(),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    private ResponseEntity<ApplicationResponse<?>> createErrorResponse(int code, String message, HttpStatus status) {
        ApplicationResponse<?> response = ApplicationResponse.error(code, message, status);
        return new ResponseEntity<>(response, status);
    }

    /**
     * Process a payment for a booking
     */
    @PostMapping("/process")
    @RateLimiter(name = "bookingPaymentProcessing", fallbackMethod = "paymentRateLimitFallback")
    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> processPayment(
            @Valid @RequestBody BookingPaymentRequest paymentRequest,
            HttpSession session) {
        final String TAG = "processPayment";
        ApiLogger.info(buildLogTag(TAG), "Received payment request for booking: " + paymentRequest.getBookingId());

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<BookingPaymentResponse> errorResponse = ApplicationResponse.error(
                    ApiCode.INVALID_TOKEN.getCode(),
                    "Authentication required",
                    ApiCode.INVALID_TOKEN.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            ApplicationResponse<BookingPaymentResponse> response = 
                paymentService.processPayment(paymentRequest, session);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error processing payment: " + e.getMessage(), e);
            ApplicationResponse<BookingPaymentResponse> errorResponse = ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to process payment: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Process a refund for a booking payment
     */
    @PostMapping("/{paymentId}/refund")
    @RateLimiter(name = "bookingPaymentRefund", fallbackMethod = "refundRateLimitFallback")
    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> processRefund(
            @PathVariable Long paymentId,
            @RequestParam BigDecimal refundAmount,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        final String TAG = "processRefund";
        ApiLogger.info(buildLogTag(TAG), "Received refund request for payment: " + paymentId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<BookingPaymentResponse> errorResponse = ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication failed",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            ApplicationResponse<BookingPaymentResponse> response = 
                paymentService.processRefund(paymentId, refundAmount, reason, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error processing refund: " + e.getMessage(), e);
            ApplicationResponse<BookingPaymentResponse> errorResponse = ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to process refund: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> getPaymentById(
            @PathVariable Long paymentId,
            HttpSession session) {
        final String TAG = "getPaymentById";
        ApiLogger.info(buildLogTag(TAG), "Received request to get payment: " + paymentId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<BookingPaymentResponse> errorResponse = ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication failed",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            ApplicationResponse<BookingPaymentResponse> response = 
                paymentService.getPaymentById(paymentId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving payment: " + e.getMessage(), e);
            ApplicationResponse<BookingPaymentResponse> errorResponse = ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve payment: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get payments for a booking
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApplicationResponse<List<BookingPaymentResponse>>> getBookingPayments(
            @PathVariable Long bookingId,
            HttpSession session) {
        final String TAG = "getBookingPayments";
        ApiLogger.info(buildLogTag(TAG), "Received request to get payments for booking: " + bookingId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<List<BookingPaymentResponse>> errorResponse = ApplicationResponse.error(
                    ApiCode.INVALID_TOKEN.getCode(),
                    "Authentication required",
                    ApiCode.INVALID_TOKEN.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            ApplicationResponse<List<BookingPaymentResponse>> response = 
                paymentService.getBookingPayments(bookingId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving booking payments: " + e.getMessage(), e);
            ApplicationResponse<List<BookingPaymentResponse>> errorResponse = ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve booking payments: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get user's payment history
     */
    @GetMapping("/user/history")
    public ResponseEntity<ApplicationResponse<PaginationResult<BookingPaymentResponse>>> getUserPaymentHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpSession session) {
        final String TAG = "getUserPaymentHistory";
        ApiLogger.info(buildLogTag(TAG), "Received request to get user payment history");

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<PaginationResult<BookingPaymentResponse>> errorResponse = ApplicationResponse.error(
                    ApiCode.INVALID_TOKEN.getCode(),
                    "Authentication required",
                    ApiCode.INVALID_TOKEN.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
            
            ApplicationResponse<PaginationResult<BookingPaymentResponse>> response = 
                paymentService.getUserPaymentHistory(pageable, status, startDate, endDate, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving payment history: " + e.getMessage(), e);
            ApplicationResponse<PaginationResult<BookingPaymentResponse>> errorResponse = ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve payment history: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get payment summary for a booking
     */
    @GetMapping("/booking/{bookingId}/summary")
    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> getBookingPaymentSummary(
            @PathVariable Long bookingId,
            HttpSession session) {
        final String TAG = "getBookingPaymentSummary";
        ApiLogger.info(buildLogTag(TAG), "Received request to get payment summary for booking: " + bookingId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<BookingPaymentResponse> errorResponse = ApplicationResponse.error(
                    ApiCode.INVALID_TOKEN.getCode(),
                    "Authentication required",
                    ApiCode.INVALID_TOKEN.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            ApplicationResponse<BookingPaymentResponse> response = 
                paymentService.getPaymentSummary(bookingId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving payment summary: " + e.getMessage(), e);
            ApplicationResponse<BookingPaymentResponse> errorResponse = ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve payment summary: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get pending payments for user
     */
    @GetMapping("/user/pending")
    public ResponseEntity<ApplicationResponse<List<BookingPaymentResponse>>> getPendingPayments(
            HttpSession session) {
        final String TAG = "getPendingPayments";
        ApiLogger.info(buildLogTag(TAG), "Received request to get pending payments");

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<List<BookingPaymentResponse>> errorResponse = ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication failed",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            ApplicationResponse<List<BookingPaymentResponse>> response = 
                paymentService.getPendingPayments(session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving pending payments: " + e.getMessage(), e);
            ApplicationResponse<List<BookingPaymentResponse>> errorResponse = ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve pending payments: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get overdue payments for user
     */
    @GetMapping("/user/overdue")
    public ResponseEntity<ApplicationResponse<List<BookingPaymentResponse>>> getOverduePayments(
            HttpSession session) {
        final String TAG = "getOverduePayments";
        ApiLogger.info(buildLogTag(TAG), "Received request to get overdue payments");

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            ApplicationResponse<List<BookingPaymentResponse>> errorResponse = ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication failed",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            ApplicationResponse<List<BookingPaymentResponse>> response = 
                paymentService.getOverduePayments(session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving overdue payments: " + e.getMessage(), e);
            ApplicationResponse<List<BookingPaymentResponse>> errorResponse = ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve overdue payments: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Verify payment status
     */
    @PostMapping("/{paymentId}/verify")
    @RateLimiter(name = "paymentVerification", fallbackMethod = "verificationRateLimitFallback")
    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> verifyPaymentStatus(
            @PathVariable Long paymentId,
            HttpSession session) {
        final String TAG = "verifyPaymentStatus";
        ApiLogger.info(buildLogTag(TAG), "Received request to verify payment status: " + paymentId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<BookingPaymentResponse> response = 
                paymentService.verifyPaymentStatus(paymentId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error verifying payment status: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to verify payment status: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Cancel a pending payment
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        final String TAG = "cancelPayment";
        ApiLogger.info(buildLogTag(TAG), "Received request to cancel payment: " + paymentId);

        ResponseEntity<ApplicationResponse<?>> authCheck = verifyAuthentication(session);
        if (authCheck != null) {
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    "Authentication required",
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
                ),
                HttpStatus.UNAUTHORIZED
            );
        }

        try {
            ApplicationResponse<BookingPaymentResponse> response = 
                paymentService.cancelPayment(paymentId, reason, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error canceling payment: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to cancel payment: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    // Rate limit fallback methods
    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> paymentRateLimitFallback(
            BookingPaymentRequest paymentRequest, HttpSession session, Exception ex) {
        ApiLogger.warn(buildLogTag("paymentRateLimitFallback"), "Payment processing rate limit exceeded");
        return new ResponseEntity<>(
            ApplicationResponse.error(
                ApiCode.RATE_LIMIT_EXCEEDED.getCode(),
                "Payment processing rate limit exceeded. Please try again later.",
                ApiCode.RATE_LIMIT_EXCEEDED.getHttpStatus()
            ),
            HttpStatus.TOO_MANY_REQUESTS
        );
    }

    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> refundRateLimitFallback(
            Long paymentId, BigDecimal refundAmount, String reason, HttpSession session, Exception ex) {
        ApiLogger.warn(buildLogTag("refundRateLimitFallback"), "Refund processing rate limit exceeded");
        return new ResponseEntity<>(
            ApplicationResponse.error(
                ApiCode.RATE_LIMIT_EXCEEDED.getCode(),
                "Refund processing rate limit exceeded. Please try again later.",
                ApiCode.RATE_LIMIT_EXCEEDED.getHttpStatus()
            ),
            HttpStatus.TOO_MANY_REQUESTS
        );
    }

    public ResponseEntity<ApplicationResponse<BookingPaymentResponse>> verificationRateLimitFallback(
            Long paymentId, HttpSession session, Exception ex) {
        ApiLogger.warn(buildLogTag("verificationRateLimitFallback"), "Payment verification rate limit exceeded");
        return new ResponseEntity<>(
            ApplicationResponse.error(
                ApiCode.RATE_LIMIT_EXCEEDED.getCode(),
                "Payment verification rate limit exceeded. Please try again later.",
                ApiCode.RATE_LIMIT_EXCEEDED.getHttpStatus()
            ),
            HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
