package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ConflictException;
import com.imovel.api.model.AuthDetails;
import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.RoleReference;
import com.imovel.api.repository.RoleRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.security.PasswordManager;
import com.imovel.api.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    @Autowired
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AuthDetailsService authDetailsService,
                       PasswordManager passwordManager
                       )
    {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authDetailsService = authDetailsService;
        this.passwordManager = passwordManager;
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

        ApiLogger.info("new user",newUser.getId());
        // Create and save authentication details for the new user
        AuthDetails authDetails = passwordManager.createAuthDetails(request.getPassword());
        authDetails.setUserId(newUser.getId());
        authDetailsService.save(authDetails);

        ApiLogger.info("new authDetails",authDetails.getId());

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
     * @param changePasswordRequestDto The DTO containing password change details
     * @return StandardResponse indicating the result of the operation
     */
    @Transactional
    public ApplicationResponse<UserResponse> changeUserPassword(PasswordChangeRequest changePasswordRequestDto) {
        ApiLogger.debug("AuthService.changeUserPassword", "Attempting to change password", changePasswordRequestDto.getEmail());

        Optional<UserResponse> userOptional = UserResponse.parse(userRepository.findByEmail(changePasswordRequestDto.getEmail()).get());

        if (userOptional.isPresent()) {
            ApiLogger.info("AuthService.changeUserPassword", "Password changed successfully", changePasswordRequestDto.getEmail());
        } else {
            ApiLogger.error("AuthService.changeUserPassword", "User not found for password change", changePasswordRequestDto.getEmail());
        }

        return  ApplicationResponse.success(null, "Password changed successfully");

    }

    public ApplicationResponse<Boolean> verifyUserCredentials(final long userId, final String password){

        return authDetailsService.verifyUserCredentials(userId, password);


    }
}