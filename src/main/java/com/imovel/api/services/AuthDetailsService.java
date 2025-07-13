package com.imovel.api.services;

import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.AuthDetails;
import com.imovel.api.repository.AuthDetailRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.security.PasswordManager;
import org.springframework.stereotype.Service;

/**
 * Service layer for authentication details operations.
 * Handles business logic related to user authentication credentials.
 */
@Service
public class AuthDetailsService {

    private final AuthDetailRepository authDetailRepository;
    private final PasswordManager passwordManager;
    private static final String SERVICE_NAME = "AuthDetailsService";

    /**
     * Constructs an AuthDetailsService with required dependencies.
     *
     * @param authDetailRepository Repository for authentication details persistence
     * @param passwordManager Utility for password hashing and verification
     */
    public AuthDetailsService(final AuthDetailRepository authDetailRepository,
                              final PasswordManager passwordManager) {
        this.authDetailRepository = authDetailRepository;
        this.passwordManager = passwordManager;
        safeLogInfo("Service initialized");
    }

    /**
     * Saves authentication details to the repository.
     *
     * @param authDetails The authentication details to be saved
     * @return StandardResponse containing the saved AuthDetails
     */
    public ApplicationResponse<AuthDetails> save(final AuthDetails authDetails) {
        safeLogDebug("Saving auth details for user: " + authDetails.getUserId());
        AuthDetails savedDetails = authDetailRepository.save(authDetails);
        safeLogInfo("Auth details saved successfully for user: " + authDetails.getUserId());
        return ApplicationResponse.success(savedDetails);
    }

    /**
     * Retrieves authentication details by user ID.
     *
     * @param id The user ID to search for
     * @return StandardResponse containing AuthDetails if found
     * @throws ResourceNotFoundException if no auth details found for user
     */
    public ApplicationResponse<AuthDetails> findByUserId(final long id) {
        safeLogDebug("Finding auth details by user ID: " + id);
        return authDetailRepository.findByUserId(id)
                .map(ApplicationResponse::success)
                .orElseThrow(() -> {
                    safeLogError("AuthDetails not found for user ID: " + id);
                    return new ResourceNotFoundException("AuthDetails", id);
                });
    }

    /**
     * Verifies if the provided credentials match the stored credentials for a user.
     *
     * @param userId The ID of the user to verify
     * @param password The plaintext password to verify
     * @return StandardResponse with verification result
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    public ApplicationResponse<Boolean> verifyUserCredentials(final long userId, final String password) {
        safeLogDebug("Verifying credentials for user ID: " + userId);
        AuthDetails authDetails = authDetailRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    safeLogError("AuthDetails not found for user ID: " + userId);
                    return new ResourceNotFoundException("AuthDetails", userId);
                });

        boolean isValid = isPasswordValid(authDetails, password);
        safeLogInfo("Verification result for user ID: " + userId + " - " + isValid);
        return ApplicationResponse.success(isValid);
    }

    /**
     * Validates if the provided password matches the stored credentials.
     *
     * @param authDetails The authentication details containing hash and salt
     * @param password The plaintext password to verify
     * @return true if password matches, false otherwise
     */
    private boolean isPasswordValid(final AuthDetails authDetails, final String password) {
        safeLogDebug("Validating password for user ID: " + authDetails.getUserId());
        boolean isValid = passwordManager.verifyPassword(
                password,
                authDetails.getHash(),
                authDetails.getSalt());
        safeLogDebug("Password validation result for user ID: " + authDetails.getUserId() + " - " + isValid);
        return isValid;
    }

    // Safe logging methods to prevent NPEs
    private void safeLogDebug(String message) {
        try {
            ApiLogger.debug(SERVICE_NAME, message);
        } catch (Exception e) {
            // Fallback to prevent logging from breaking functionality
            System.out.println("[DEBUG] " + SERVICE_NAME + " - " + message);
        }
    }

    private void safeLogInfo(String message) {
        try {
            ApiLogger.info(SERVICE_NAME, message);
        } catch (Exception e) {
            System.out.println("[INFO] " + SERVICE_NAME + " - " + message);
        }
    }

    private void safeLogError(String message) {
        try {
            ApiLogger.error(SERVICE_NAME, message);
        } catch (Exception e) {
            System.out.println("[ERROR] " + SERVICE_NAME + " - " + message);
        }
    }
}