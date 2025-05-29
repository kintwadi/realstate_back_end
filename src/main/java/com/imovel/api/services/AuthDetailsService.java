package com.imovel.api.services;

import com.imovel.api.model.AuthDetails;
import com.imovel.api.repository.AuthDetailRepository;
import com.imovel.api.security.PasswordManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for authentication details operations.
 * Handles business logic related to user authentication credentials.
 */
@Service
public class AuthDetailsService {

    private final AuthDetailRepository authDetailRepository;
    private final PasswordManager passwordManager;

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
    }

    /**
     * Saves authentication details to the repository.
     *
     * @param authDetails The authentication details to be saved
     */
    public AuthDetails save(final AuthDetails authDetails) {
        return  authDetailRepository.save(authDetails);
    }

    /**
     * Retrieves authentication details by user ID.
     *
     * @param id The user ID to search for
     * @return Optional containing AuthDetails if found, empty otherwise
     */
    public Optional<AuthDetails> findByUserId(final long id) {
        return authDetailRepository.findByUserId(id);
    }

    /**
     * Verifies if the provided credentials match the stored credentials for a user.
     *
     * @param userId The ID of the user to verify
     * @param password The plaintext password to verify
     * @return true if credentials are valid, false otherwise
     * @throws IllegalArgumentException if no user exists with the given ID
     */
    public boolean verifyUserCredentials(final long userId, final String password) {
        return findByUserId(userId)
                .map(authDetails -> isPasswordValid(authDetails, password))
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    /**
     * Validates if the provided password matches the stored credentials.
     *
     * @param authDetails The authentication details containing hash and salt
     * @param password The plaintext password to verify
     * @return true if password matches, false otherwise
     */
    private boolean isPasswordValid(final AuthDetails authDetails, final String password) {
        return passwordManager.verifyPassword(
                password,
                authDetails.getHash(),
                authDetails.getSalt());
    }
}