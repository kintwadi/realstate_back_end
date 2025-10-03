package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ConflictException;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.model.AuthDetails;
import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.RoleReference;
import com.imovel.api.repository.RoleRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.ChangePasswordRequest;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.security.PasswordManager;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.util.Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Service for handling authentication-related operations.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthDetailsService authDetailsService;
    private final PasswordManager passwordManager;
    private final JWTProvider jwtProvider;

    @Autowired
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AuthDetailsService authDetailsService,
                       PasswordManager passwordManager, JWTProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authDetailsService = authDetailsService;
        this.passwordManager = passwordManager;
        this.jwtProvider = jwtProvider;
    }

    private String buildLogTag(String method) {
        return "AuthService." + method;
    }

    /**
     * Registers a new user in the system.
     *
     * @param request The user registration request
     * @return StandardResponse containing the registered user
     * @throws ConflictException if email is already registered
     */
    @Transactional
    public ApplicationResponse<UserResponse> registerUser(UserRegistrationRequest request) {
        ApiLogger.debug("AuthService.registerUser", "Attempting to register user", request.getEmail());

        // Validate password
        if (request.getPassword().isBlank()) {
            return ApplicationResponse.error(ApiCode.INVALID_CREDENTIALS.getCode(),
                    ApiCode.INVALID_CREDENTIALS.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
//        // Validate email format
        if (Util.isEmailInvalid(request.getEmail())) {
            return ApplicationResponse.error(ApiCode.INVALID_EMAIL.getCode(),
                    ApiCode.INVALID_EMAIL.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            ApiLogger.error("AuthService.registerUser", "Email already exists", request.getEmail());
            return ApplicationResponse.error(ApiCode.INVALID_EMAIL_ALREADY_EXIST.getCode(),
                    ApiCode.INVALID_EMAIL_ALREADY_EXIST.getMessage(),
                    HttpStatus.NOT_FOUND);
        }

        Role role = roleRepository.findByRoleName(RoleReference.TENANT.name())
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName(RoleReference.TENANT.name());
                    return roleRepository.save(newRole);

                });


        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setRole(role);

        newUser = userRepository.save(newUser);

        ApiLogger.info("new user", newUser.getId());
        // Create and save authentication details for the new user
        AuthDetails authDetails = passwordManager.createAuthDetails(request.getPassword());
        authDetails.setUserId(newUser.getId());
        authDetailsService.save(authDetails);

        ApiLogger.info("new authDetails", authDetails.getId());

        UserResponse userResponse = UserResponse.parse(newUser).get();

        ApiLogger.info("AuthService.registerUser", "User registered successfully");
        return ApplicationResponse.success(userResponse, "User registered successfully");
    }

    /**
     * Finds a user by email.
     *
     * @param email The email to search for
     * @return StandardResponse containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(final String email) {
        ApiLogger.debug("AuthService.findByEmail", "Looking for user by email", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            ApiLogger.debug("AuthService.findByEmail", "User found", email);
        } else {
            ApiLogger.debug("AuthService.findByEmail", "User not found", email);
        }
        return user;
    }

    /**
     * Changes the user's password after validating the request.
     *
     * @param request The DTO containing password change details
     * @return StandardResponse indicating the result of the operation
     */
    @Transactional
    public ApplicationResponse<Void> changePassword(final ChangePasswordRequest request,
                                                    final Long userId) {
        ApiLogger.debug(buildLogTag("changePassword"), "Attempting to change password");

        if (request == null) {
            return ApplicationResponse.error(
                    ApiCode.INVALID_PAYLOAD.getCode(),
                    ApiCode.INVALID_PAYLOAD.getMessage(),
                    ApiCode.INVALID_PAYLOAD.getHttpStatus()
            );
        }
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()
                || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ApplicationResponse.error(
                    ApiCode.MISSING_FIELDS.getCode(),
                    ApiCode.MISSING_FIELDS.getMessage(),
                    ApiCode.MISSING_FIELDS.getHttpStatus()
            );
        }
        if (userId == null) {
            ApiLogger.error(buildLogTag("changePassword"), "Null userId passed from controller");
            return ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    ApiCode.AUTHENTICATION_FAILED.getMessage(),
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
            );
        }

        try {
            // Fetch existing auth details via your service (returns ApplicationResponse<AuthDetails>)
            final AuthDetails auth;
            try {
                auth = authDetailsService.findByUserId(userId).getData();
            } catch (ResourceNotFoundException ex) {
                ApiLogger.error(buildLogTag("changePassword"), "AuthDetails not found for user: " + userId);
                return ApplicationResponse.error(
                        ApiCode.USER_NOT_FOUND.getCode(),
                        ApiCode.USER_NOT_FOUND.getMessage(),
                        ApiCode.USER_NOT_FOUND.getHttpStatus()
                );
            }

            // Verify current password
            boolean ok = passwordManager.verifyPassword(
                    request.getCurrentPassword(),
                    auth.getHash(),
                    auth.getSalt()
            );
            if (!ok) {
                ApiLogger.error(buildLogTag("changePassword"), "Current password mismatch for user: " + userId);
                return ApplicationResponse.error(
                        ApiCode.INAVALID_PASSWORD.getCode(), // (enum spelling kept)
                        ApiCode.INAVALID_PASSWORD.getMessage(),
                        ApiCode.INAVALID_PASSWORD.getHttpStatus()
                );
            }

            // (Optional) enforce policy here…
            // if (request.getNewPassword().length() < 8) { ... }

            // Hash new password and save
            AuthDetails fresh = passwordManager.createAuthDetails(request.getNewPassword());
            auth.setSalt(fresh.getSalt());
            auth.setHash(fresh.getHash());
            authDetailsService.save(auth); // we don't use the returned body

            ApiLogger.info(buildLogTag("changePassword"), "Password changed for user: " + userId);
            return ApplicationResponse.success(null, "Password changed successfully");

        } catch (Exception e) {
            ApiLogger.error(buildLogTag("changePassword"), e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    ApiCode.SYSTEM_ERROR.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }


    public ApplicationResponse<Boolean> verifyUserCredentials(final long userId, final String password) {
        return authDetailsService.verifyUserCredentials(userId, password);
    }
}