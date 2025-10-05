package com.imovel.api.booking.service;

import com.imovel.api.booking.model.Booking;
import com.imovel.api.booking.model.CancellationPolicy;
import com.imovel.api.booking.model.enums.CancellationPolicyType;
import com.imovel.api.booking.repository.CancellationPolicyRepository;
import com.imovel.api.booking.request.CancellationPolicyRequest;
import com.imovel.api.booking.response.CancellationPolicyResponse;
import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.Property;
import com.imovel.api.model.User;
import com.imovel.api.repository.PropertyRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.session.SessionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing cancellation policies.
 * Handles policy creation, updates, and refund calculations.
 */
@Service
@Transactional
public class CancellationPolicyService {

    private final CancellationPolicyRepository policyRepository;
    private final PropertyRepository propertyRepository;
    private final SessionManager sessionManager;

    private static final String SERVICE_NAME = "CancellationPolicyService";

    @Autowired
    public CancellationPolicyService(CancellationPolicyRepository policyRepository,
                                   PropertyRepository propertyRepository,
                                   SessionManager sessionManager) {
        this.policyRepository = policyRepository;
        this.propertyRepository = propertyRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * Creates or updates a cancellation policy for a property.
     */
    public ApplicationResponse<CancellationPolicyResponse> createOrUpdatePolicy(
            CancellationPolicyRequest request, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Creating/updating cancellation policy for property: " + request.getPropertyId());

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            // Validate property exists and user owns it
            Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", request.getPropertyId()));

            if (!property.getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to modify policies for this property", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            // Check if policy already exists for this property
            List<CancellationPolicy> existingPolicies = policyRepository.findByPropertyId(request.getPropertyId());
            Optional<CancellationPolicy> existingPolicy = existingPolicies.isEmpty() ? Optional.empty() : Optional.of(existingPolicies.get(0));

            CancellationPolicy policy;
            if (existingPolicy.isPresent()) {
                policy = existingPolicy.get();
                ApiLogger.info(SERVICE_NAME, "Updating existing policy: " + policy.getId());
            } else {
                policy = new CancellationPolicy();
                policy.setProperty(property);
                ApiLogger.info(SERVICE_NAME, "Creating new policy for property: " + request.getPropertyId());
            }

            // Update policy fields
            policy.setPolicyType(request.getPolicyType());
            policy.setRefundPercentage(request.getRefundPercentage());
            policy.setDaysBeforeCheckin(request.getDaysBeforeCheckIn());
            policy.setDescription(request.getDescription());
            policy.setIsActive(request.getIsActive());

            // Validate policy consistency
            ApplicationResponse<String> validationResult = validatePolicyConsistency(policy);
            if (!validationResult.isSuccess()) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), validationResult.getMessage(), ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            policy = policyRepository.save(policy);

            ApiLogger.info(SERVICE_NAME, "Successfully saved cancellation policy: " + policy.getId());

            return ApplicationResponse.success(convertToPolicyResponse(policy));

        } catch (ResourceNotFoundException e) {
            ApiLogger.error(SERVICE_NAME, "Property not found: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error creating/updating policy: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to save cancellation policy", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Creates a cancellation policy (delegates to createOrUpdatePolicy)
     */
    @Transactional
    public ApplicationResponse<CancellationPolicyResponse> createPolicy(CancellationPolicyRequest request, HttpSession session) {
        return createOrUpdatePolicy(request, session);
    }

    /**
     * Retrieves the cancellation policy for a property.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<CancellationPolicyResponse> getPropertyPolicy(Long propertyId) {
        try {
            // Validate property exists
            Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

            List<CancellationPolicy> policies = policyRepository.findByPropertyId(propertyId);
            Optional<CancellationPolicy> policy = policies.isEmpty() ? Optional.empty() : Optional.of(policies.get(0));

            if (policy.isPresent()) {
                return ApplicationResponse.success(convertToPolicyResponse(policy.get()));
            } else {
                // Return default policy if none exists
                CancellationPolicyResponse defaultPolicy = createDefaultPolicyResponse(propertyId);
                return ApplicationResponse.success(defaultPolicy);
            }

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving policy: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve cancellation policy", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Calculates refund amount based on cancellation policy and timing.
     */
    public ApplicationResponse<BigDecimal> calculateRefund(Booking booking, LocalDate cancellationDate) {
        try {
            ApiLogger.info(SERVICE_NAME, "Calculating refund for booking: " + booking.getId());

            // Get cancellation policy for the property
            List<CancellationPolicy> policyList = policyRepository.findByPropertyId(booking.getProperty().getId());
            Optional<CancellationPolicy> policyOpt = policyList.isEmpty() ? Optional.empty() : Optional.of(policyList.get(0));

            CancellationPolicy policy;
            if (policyOpt.isPresent() && policyOpt.get().getIsActive()) {
                policy = policyOpt.get();
            } else {
                // Use default moderate policy
                policy = createDefaultPolicy();
            }

            // Calculate days between cancellation and check-in
            long daysUntilCheckIn = ChronoUnit.DAYS.between(cancellationDate, booking.getCheckInDate());

            BigDecimal refundAmount = BigDecimal.ZERO;
            BigDecimal totalAmount = booking.getTotalAmount();

            // Apply policy based on timing
            if (daysUntilCheckIn >= policy.getDaysBeforeCheckin()) {
                // Full or partial refund based on policy
                BigDecimal refundPercentage = policy.getRefundPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                refundAmount = totalAmount.multiply(refundPercentage).setScale(2, RoundingMode.HALF_UP);
            } else {
                // Apply reduced refund based on policy type
                switch (policy.getPolicyType()) {
                    case FLEXIBLE:
                        if (daysUntilCheckIn >= 1) {
                            refundAmount = totalAmount.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
                        }
                        break;
                    case MODERATE:
                        if (daysUntilCheckIn >= 5) {
                            refundAmount = totalAmount.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
                        }
                        break;
                    case STRICT:
                        // No refund for late cancellations
                        refundAmount = BigDecimal.ZERO;
                        break;
                    case SUPER_STRICT_30:
                    case SUPER_STRICT_60:
                        // No refund for any cancellation after booking
                        refundAmount = BigDecimal.ZERO;
                        break;
                    case NON_REFUNDABLE:
                        // No refund for non-refundable bookings
                        refundAmount = BigDecimal.ZERO;
                        break;
                }
            }

            // Ensure refund doesn't exceed total amount
            if (refundAmount.compareTo(totalAmount) > 0) {
                refundAmount = totalAmount;
            }

            ApiLogger.info(SERVICE_NAME, String.format("Calculated refund: %s for booking: %d (Policy: %s, Days until check-in: %d)", 
                refundAmount, booking.getId(), policy.getPolicyType(), daysUntilCheckIn));

            return ApplicationResponse.success(refundAmount);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error calculating refund: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to calculate refund", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Calculates refund amount based on policy ID, total amount, and check-in date.
     */
    public ApplicationResponse<BigDecimal> calculateRefund(Long policyId, BigDecimal totalAmount, LocalDateTime checkInDate, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Calculating refund for policy: " + policyId);

            // Get the cancellation policy
            Optional<CancellationPolicy> policyOpt = policyRepository.findById(policyId);
            if (!policyOpt.isPresent()) {
                return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), "Cancellation policy not found", ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
            }

            CancellationPolicy policy = policyOpt.get();
            if (!policy.getIsActive()) {
                return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), "Policy is not active", ApiCode.VALIDATION_ERROR.getHttpStatus());
            }

            // Calculate days between now and check-in
            long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkInDate.toLocalDate());

            BigDecimal refundAmount = BigDecimal.ZERO;

            // Apply policy based on timing
            if (daysUntilCheckIn >= policy.getDaysBeforeCheckin()) {
                // Full or partial refund based on policy
                BigDecimal refundPercentage = policy.getRefundPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                refundAmount = totalAmount.multiply(refundPercentage).setScale(2, RoundingMode.HALF_UP);
            } else {
                // Apply reduced refund based on policy type
                switch (policy.getPolicyType()) {
                    case FLEXIBLE:
                        if (daysUntilCheckIn >= 1) {
                            refundAmount = totalAmount.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
                        }
                        break;
                    case MODERATE:
                        if (daysUntilCheckIn >= 5) {
                            refundAmount = totalAmount.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
                        }
                        break;
                    case STRICT:
                    case SUPER_STRICT_30:
                    case SUPER_STRICT_60:
                    case NON_REFUNDABLE:
                        // No refund for strict policies or late cancellations
                        refundAmount = BigDecimal.ZERO;
                        break;
                }
            }

            // Ensure refund doesn't exceed total amount
            if (refundAmount.compareTo(totalAmount) > 0) {
                refundAmount = totalAmount;
            }

            ApiLogger.info(SERVICE_NAME, String.format("Calculated refund: %s for policy: %d (Days until check-in: %d)", 
                refundAmount, policyId, daysUntilCheckIn));

            return ApplicationResponse.success(refundAmount);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error calculating refund: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to calculate refund", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Gets default cancellation policy templates.
     */
    public ApplicationResponse<List<CancellationPolicyResponse>> getDefaultPolicies(HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Getting default cancellation policies");

            List<CancellationPolicyResponse> defaultPolicies = List.of(
                createDefaultPolicyResponse(CancellationPolicyType.FLEXIBLE),
                createDefaultPolicyResponse(CancellationPolicyType.MODERATE),
                createDefaultPolicyResponse(CancellationPolicyType.STRICT),
                createDefaultPolicyResponse(CancellationPolicyType.SUPER_STRICT_30),
                createDefaultPolicyResponse(CancellationPolicyType.SUPER_STRICT_60),
                createDefaultPolicyResponse(CancellationPolicyType.NON_REFUNDABLE)
            );

            return ApplicationResponse.success(defaultPolicies);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error getting default policies: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to get default policies", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    private CancellationPolicyResponse createDefaultPolicyResponse(CancellationPolicyType policyType) {
        CancellationPolicyResponse response = new CancellationPolicyResponse();
        response.setPolicyType(policyType);
        response.setRefundPercentage(policyType.getDefaultRefundPercentage());
        response.setDaysBeforeCheckIn(policyType.getDefaultDaysBeforeCheckin());
        response.setDescription(policyType.getDescription());
        response.setIsActive(true);
        return response;
    }

    /**
     * Validates a cancellation policy request.
     */
    public ApplicationResponse<String> validatePolicy(CancellationPolicyRequest policyRequest, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Validating cancellation policy");

            // Create a temporary policy object for validation
            CancellationPolicy tempPolicy = new CancellationPolicy();
            tempPolicy.setPolicyType(policyRequest.getPolicyType());
            tempPolicy.setRefundPercentage(policyRequest.getRefundPercentage());
            tempPolicy.setDaysBeforeCheckin(policyRequest.getDaysBeforeCheckIn());
            tempPolicy.setDescription(policyRequest.getDescription());

            // Use the existing validation method
            ApplicationResponse<String> validationResult = validatePolicyConsistency(tempPolicy);
            
            if (validationResult.isSuccess()) {
                return ApplicationResponse.success("Policy is valid and consistent");
            } else {
                return validationResult;
            }

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error validating policy: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to validate policy", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Gets a cancellation policy by ID.
     */
    public ApplicationResponse<CancellationPolicyResponse> getPolicyById(Long policyId, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Getting cancellation policy by ID: " + policyId);

            Optional<CancellationPolicy> policyOpt = policyRepository.findById(policyId);
            if (!policyOpt.isPresent()) {
                return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), "Cancellation policy not found", ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
            }

            CancellationPolicy policy = policyOpt.get();
            
            // Verify user has access to this policy (either owns the property or is admin)
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);
            if (currentUser == null) {
                return ApplicationResponse.error(ApiCode.AUTHENTICATION_FAILED.getCode(), "User not authenticated", ApiCode.AUTHENTICATION_FAILED.getHttpStatus());
            }

            // Check if user owns the property or is admin
            if (!policy.getProperty().getCreatedBy().getId().equals(currentUser.getId()) && 
                !currentUser.getRole().getRoleName().equals("ADMIN")) {
                return ApplicationResponse.error(ApiCode.ACCESS_DENIED.getCode(), "Access denied", ApiCode.ACCESS_DENIED.getHttpStatus());
            }

            CancellationPolicyResponse response = convertToPolicyResponse(policy);
            return ApplicationResponse.success(response);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error getting policy by ID: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to get policy", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Gets cancellation policies for a specific property.
     */
    public ApplicationResponse<List<CancellationPolicyResponse>> getPropertyPolicies(Long propertyId, boolean activeOnly, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Getting policies for property: " + propertyId + ", activeOnly: " + activeOnly);

            // Verify user has access to this property
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);
            if (currentUser == null) {
                return ApplicationResponse.error(ApiCode.AUTHENTICATION_FAILED.getCode(), "User not authenticated", ApiCode.AUTHENTICATION_FAILED.getHttpStatus());
            }

            // Get the property to verify ownership
            Optional<Property> propertyOpt = propertyRepository.findById(propertyId);
            if (!propertyOpt.isPresent()) {
                return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), "Property not found", ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
            }

            Property property = propertyOpt.get();
            
            // Check if user owns the property or is admin
            if (!property.getCreatedBy().getId().equals(currentUser.getId()) && 
                !currentUser.getRole().getRoleName().equals("ADMIN")) {
                return ApplicationResponse.error(ApiCode.ACCESS_DENIED.getCode(), "Access denied", ApiCode.ACCESS_DENIED.getHttpStatus());
            }

            // Get policies for the property
            List<CancellationPolicyResponse> policyResponses;
            if (activeOnly) {
                Optional<CancellationPolicy> activePolicyOpt = policyRepository.findActiveByPropertyId(propertyId);
                policyResponses = activePolicyOpt.map(cp -> List.of(convertToPolicyResponse(cp))).orElseGet(List::of);
            } else {
                List<CancellationPolicy> policies = policyRepository.findByPropertyId(propertyId);
                policyResponses = policies.stream()
                    .map(this::convertToPolicyResponse)
                    .collect(Collectors.toList());
            }

            return ApplicationResponse.success(policyResponses);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error getting property policies: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to get property policies", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Gets policy statistics for the current user.
     */
    public ApplicationResponse<PolicyStatistics> getPolicyStatistics(HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Getting policy statistics");

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);
            if (currentUser == null) {
                return ApplicationResponse.error(ApiCode.AUTHENTICATION_FAILED.getCode(), "User not authenticated", ApiCode.AUTHENTICATION_FAILED.getHttpStatus());
            }

            // Get statistics for user's policies (host-owned properties)
            List<CancellationPolicy> userPolicies = policyRepository.findByHostId(currentUser.getId());
            
            long totalPolicies = userPolicies.size();
            long activePolicies = userPolicies.stream().filter(CancellationPolicy::getIsActive).count();
            long inactivePolicies = totalPolicies - activePolicies;
            
            // Count by type
            Map<CancellationPolicyType, Long> policyTypeCount = userPolicies.stream()
                .collect(Collectors.groupingBy(CancellationPolicy::getPolicyType, Collectors.counting()));

            PolicyStatistics stats = new PolicyStatistics(
                totalPolicies,
                activePolicies,
                inactivePolicies,
                policyTypeCount
            );

            return ApplicationResponse.success(stats);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error getting policy statistics: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to get policy statistics", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Inner class for policy statistics.
     */
    public static class PolicyStatistics {
        private long totalPolicies;
        private long activePolicies;
        private long inactivePolicies;
        private Map<CancellationPolicyType, Long> policyTypeCount;

        public PolicyStatistics(long totalPolicies, long activePolicies, long inactivePolicies, Map<CancellationPolicyType, Long> policyTypeCount) {
            this.totalPolicies = totalPolicies;
            this.activePolicies = activePolicies;
            this.inactivePolicies = inactivePolicies;
            this.policyTypeCount = policyTypeCount;
        }

        // Getters
        public long getTotalPolicies() { return totalPolicies; }
        public long getActivePolicies() { return activePolicies; }
        public long getInactivePolicies() { return inactivePolicies; }
        public Map<CancellationPolicyType, Long> getPolicyTypeCount() { return policyTypeCount; }

        // Setters
        public void setTotalPolicies(long totalPolicies) { this.totalPolicies = totalPolicies; }
        public void setActivePolicies(long activePolicies) { this.activePolicies = activePolicies; }
        public void setInactivePolicies(long inactivePolicies) { this.inactivePolicies = inactivePolicies; }
        public void setPolicyTypeCount(Map<CancellationPolicyType, Long> policyTypeCount) { this.policyTypeCount = policyTypeCount; }
    }

    /**
     * Gets all cancellation policies for properties owned by the current user.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<List<CancellationPolicyResponse>> getUserPolicies(HttpSession session) {
        try {
            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            List<CancellationPolicy> policies = policyRepository.findByHostId(currentUser.getId());

            List<CancellationPolicyResponse> policyResponses = policies.stream()
                .map(this::convertToPolicyResponse)
                .collect(Collectors.toList());

            return ApplicationResponse.success(policyResponses);

        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error retrieving user policies: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to retrieve policies", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    /**
     * Deletes a cancellation policy.
     */
    public ApplicationResponse<Void> deletePolicy(Long policyId, HttpSession session) {
        try {
            ApiLogger.info(SERVICE_NAME, "Deleting cancellation policy: " + policyId);

            User currentUser = sessionManager.getCurrentAuthenticatedUser(session);

            CancellationPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("CancellationPolicy", policyId));

            // Check permissions
            if (!policy.getProperty().getCreatedBy().getId().equals(currentUser.getId())) {
                return ApplicationResponse.error(ApiCode.PERMISSION_DENIED.getCode(), "Not authorized to delete this policy", ApiCode.PERMISSION_DENIED.getHttpStatus());
            }

            policyRepository.delete(policy);

            ApiLogger.info(SERVICE_NAME, "Successfully deleted cancellation policy: " + policyId);

            return ApplicationResponse.success(null);

        } catch (ResourceNotFoundException e) {
            return ApplicationResponse.error(ApiCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage(), ApiCode.RESOURCE_NOT_FOUND.getHttpStatus());
        } catch (Exception e) {
            ApiLogger.error(SERVICE_NAME, "Error deleting policy: " + e.getMessage());
            return ApplicationResponse.error(ApiCode.SYSTEM_ERROR.getCode(), "Failed to delete policy", ApiCode.SYSTEM_ERROR.getHttpStatus());
        }
    }

    // Helper methods

    private ApplicationResponse<String> validatePolicyConsistency(CancellationPolicy policy) {
        // Validate policy type and refund percentage consistency
        switch (policy.getPolicyType()) {
            case FLEXIBLE:
                if (policy.getRefundPercentage().compareTo(BigDecimal.valueOf(80)) < 0) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Flexible policy should offer at least 80% refund", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                if (policy.getDaysBeforeCheckin() > 1) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Flexible policy should require cancellation at most 1 day before check-in", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                break;
            case MODERATE:
                if (policy.getRefundPercentage().compareTo(BigDecimal.valueOf(50)) < 0) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Moderate policy should offer at least 50% refund", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                if (policy.getDaysBeforeCheckin() < 5 || policy.getDaysBeforeCheckin() > 7) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Moderate policy should require cancellation 5-7 days before check-in", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                break;
            case STRICT:
                if (policy.getRefundPercentage().compareTo(BigDecimal.valueOf(50)) > 0) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Strict policy should offer at most 50% refund", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                if (policy.getDaysBeforeCheckin() < 7) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Strict policy should require cancellation at least 7 days before check-in", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                break;
            case SUPER_STRICT_30:
            case SUPER_STRICT_60:
                if (policy.getRefundPercentage().compareTo(BigDecimal.valueOf(50)) > 0) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Super strict policy should offer limited refund (max 50%)", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                break;
            case NON_REFUNDABLE:
                if (policy.getRefundPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    return ApplicationResponse.error(ApiCode.VALIDATION_ERROR.getCode(), 
                        "Non-refundable policy should offer no refund", ApiCode.VALIDATION_ERROR.getHttpStatus());
                }
                break;
        }

        return ApplicationResponse.success("Policy is valid");
    }

    private CancellationPolicy createDefaultPolicy() {
        CancellationPolicy defaultPolicy = new CancellationPolicy();
        defaultPolicy.setPolicyType(CancellationPolicyType.MODERATE);
        defaultPolicy.setRefundPercentage(BigDecimal.valueOf(50));
        defaultPolicy.setDaysBeforeCheckin(5);
        defaultPolicy.setDescription("Moderate cancellation policy - 50% refund if cancelled 5+ days before check-in");
        defaultPolicy.setIsActive(true);
        return defaultPolicy;
    }

    private CancellationPolicyResponse createDefaultPolicyResponse(Long propertyId) {
        CancellationPolicyResponse response = new CancellationPolicyResponse();
        response.setPropertyId(propertyId);
        response.setPolicyType(CancellationPolicyType.MODERATE);
        response.setRefundPercentage(BigDecimal.valueOf(50));
        response.setDaysBeforeCheckIn(5);
        response.setDescription("Default moderate cancellation policy - 50% refund if cancelled 5+ days before check-in");
        response.setIsActive(true);
        return response;
    }

    private CancellationPolicyResponse convertToPolicyResponse(CancellationPolicy policy) {
        CancellationPolicyResponse response = new CancellationPolicyResponse();
        response.setId(policy.getId());
        response.setPropertyId(policy.getProperty().getId());
        response.setPolicyType(policy.getPolicyType());
        response.setRefundPercentage(policy.getRefundPercentage());
        response.setDaysBeforeCheckIn(policy.getDaysBeforeCheckin());
        response.setDescription(policy.getDescription());
        response.setIsActive(policy.getIsActive());
        response.setCreatedAt(policy.getCreatedAt());
        response.setUpdatedAt(policy.getUpdatedAt());

        return response;
    }
}
