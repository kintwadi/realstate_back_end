package com.imovel.api.booking.controller;

import com.imovel.api.booking.request.CancellationPolicyRequest;
import com.imovel.api.booking.response.CancellationPolicyResponse;
import com.imovel.api.booking.service.CancellationPolicyService;
import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.pagination.PaginationResult;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.CurrentUser;
import com.imovel.api.session.SessionManager;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for handling cancellation policy operations.
 * Provides endpoints for managing property cancellation policies.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cancellation-policies")
public class CancellationPolicyController {

    private final CancellationPolicyService policyService;
    private final SessionManager sessionManager;

    @Autowired
    public CancellationPolicyController(CancellationPolicyService policyService, SessionManager sessionManager) {
        this.policyService = policyService;
        this.sessionManager = sessionManager;
    }

    private String buildLogTag(String method) {
        return "CancellationPolicyController#" + method;
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
     * Create a new cancellation policy
     */
    @PostMapping
    public ResponseEntity<ApplicationResponse<CancellationPolicyResponse>> createPolicy(
            @Valid @RequestBody CancellationPolicyRequest policyRequest,
            HttpSession session) {
        final String TAG = "createPolicy";
        ApiLogger.info(buildLogTag(TAG), "Received request to create cancellation policy for property: " + 
                policyRequest.getPropertyId());

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
            ApplicationResponse<CancellationPolicyResponse> response = 
                policyService.createPolicy(policyRequest, session);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error creating cancellation policy: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to create cancellation policy: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get cancellation policy by ID
     */
    @GetMapping("/{policyId}")
    public ResponseEntity<ApplicationResponse<CancellationPolicyResponse>> getPolicyById(
            @PathVariable Long policyId,
            HttpSession session) {
        final String TAG = "getPolicyById";
        ApiLogger.info(buildLogTag(TAG), "Received request to get cancellation policy: " + policyId);

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
            ApplicationResponse<CancellationPolicyResponse> response = 
                policyService.getPolicyById(policyId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving cancellation policy: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve cancellation policy: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get cancellation policies for a property
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApplicationResponse<List<CancellationPolicyResponse>>> getPropertyPolicies(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            HttpSession session) {
        final String TAG = "getPropertyPolicies";
        ApiLogger.info(buildLogTag(TAG), "Received request to get policies for property: " + propertyId);

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
            ApplicationResponse<List<CancellationPolicyResponse>> response = 
                policyService.getPropertyPolicies(propertyId, activeOnly, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving property policies: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve property policies: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get all cancellation policies for the current user's properties
     */
    @GetMapping("/user/properties")
    public ResponseEntity<ApplicationResponse<List<CancellationPolicyResponse>>> getUserPropertyPolicies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String policyType,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            HttpSession session) {
        final String TAG = "getUserPropertyPolicies";
        ApiLogger.info(buildLogTag(TAG), "Received request to get user property policies");

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
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
            
            ApplicationResponse<List<CancellationPolicyResponse>> response = 
                policyService.getUserPolicies(session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving user property policies: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve user property policies: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Update a cancellation policy
     */
    @PutMapping("/{policyId}")
    public ResponseEntity<ApplicationResponse<CancellationPolicyResponse>> updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody CancellationPolicyRequest policyRequest,
            HttpSession session) {
        final String TAG = "updatePolicy";
        ApiLogger.info(buildLogTag(TAG), "Received request to update cancellation policy: " + policyId);

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
            ApplicationResponse<CancellationPolicyResponse> response = 
                policyService.createOrUpdatePolicy(policyRequest, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error updating cancellation policy: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update cancellation policy: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Delete a cancellation policy
     */
    @DeleteMapping("/{policyId}")
    public ResponseEntity<ApplicationResponse<Void>> deletePolicy(
            @PathVariable Long policyId,
            HttpSession session) {
        final String TAG = "deletePolicy";
        ApiLogger.info(buildLogTag(TAG), "Received request to delete cancellation policy: " + policyId);

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
            ApplicationResponse<Void> response = policyService.deletePolicy(policyId, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error deleting cancellation policy: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to delete cancellation policy: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Activate or deactivate a cancellation policy
     */
    @PatchMapping("/{policyId}/status")
    public ResponseEntity<ApplicationResponse<CancellationPolicyResponse>> updatePolicyStatus(
            @PathVariable Long policyId,
            @RequestParam boolean isActive,
            HttpSession session) {
        final String TAG = "updatePolicyStatus";
        ApiLogger.info(buildLogTag(TAG), "Received request to update policy status: " + policyId + " to " + isActive);

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
            // Get the existing policy first
            ApplicationResponse<CancellationPolicyResponse> getResponse = 
                policyService.getPropertyPolicy(policyId);
            
            if (!getResponse.isSuccess()) {
                return new ResponseEntity<>(getResponse, HttpStatus.NOT_FOUND);
            }
            
            // Create a request to update the policy with new status
            CancellationPolicyRequest updateRequest = new CancellationPolicyRequest();
            CancellationPolicyResponse existingPolicy = getResponse.getData();
            updateRequest.setPropertyId(existingPolicy.getPropertyId());
            updateRequest.setPolicyType(existingPolicy.getPolicyType());
            updateRequest.setRefundPercentage(existingPolicy.getRefundPercentage());
            updateRequest.setDaysBeforeCheckIn(existingPolicy.getDaysBeforeCheckIn());
            updateRequest.setDescription(existingPolicy.getDescription());
            updateRequest.setIsActive(isActive); // Update the status
            
            ApplicationResponse<CancellationPolicyResponse> response = 
                policyService.createOrUpdatePolicy(updateRequest, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error updating policy status: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to update policy status: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Calculate refund amount for a booking based on cancellation policy
     */
    @GetMapping("/calculate-refund")
    public ResponseEntity<ApplicationResponse<BigDecimal>> calculateRefund(
            @RequestParam Long policyId,
            @RequestParam BigDecimal totalAmount,
            @RequestParam LocalDateTime checkInDate,
            HttpSession session) {
        final String TAG = "calculateRefund";
        ApiLogger.info(buildLogTag(TAG), "Received request to calculate refund for policy: " + policyId);

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
            ApplicationResponse<BigDecimal> response = 
                policyService.calculateRefund(policyId, totalAmount, checkInDate, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error calculating refund: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to calculate refund: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get default cancellation policies (templates)
     */
    @GetMapping("/defaults")
    public ResponseEntity<ApplicationResponse<List<CancellationPolicyResponse>>> getDefaultPolicies(
            HttpSession session) {
        final String TAG = "getDefaultPolicies";
        ApiLogger.info(buildLogTag(TAG), "Received request to get default cancellation policies");

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
            ApplicationResponse<List<CancellationPolicyResponse>> response = 
                policyService.getDefaultPolicies(session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving default policies: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve default policies: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Validate a cancellation policy
     */
    @PostMapping("/validate")
    public ResponseEntity<ApplicationResponse<String>> validatePolicy(
            @Valid @RequestBody CancellationPolicyRequest policyRequest,
            HttpSession session) {
        final String TAG = "validatePolicy";
        ApiLogger.info(buildLogTag(TAG), "Received request to validate cancellation policy");

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
            ApplicationResponse<String> response = policyService.validatePolicy(policyRequest, session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error validating policy: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to validate policy: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get policy statistics for user's properties
     */
    @GetMapping("/user/statistics")
    public ResponseEntity<ApplicationResponse<CancellationPolicyService.PolicyStatistics>> getPolicyStatistics(
            HttpSession session) {
        final String TAG = "getPolicyStatistics";
        ApiLogger.info(buildLogTag(TAG), "Received request to get policy statistics");

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
            ApplicationResponse<CancellationPolicyService.PolicyStatistics> response = 
                policyService.getPolicyStatistics(session);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiLogger.error(buildLogTag(TAG), "Error retrieving policy statistics: " + e.getMessage(), e);
            return new ResponseEntity<>(
                ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    "Failed to retrieve policy statistics: " + e.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
