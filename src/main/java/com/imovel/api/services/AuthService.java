package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ConflictException;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.UserRole;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service for handling authentication-related operations.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthDetailsService authDetailsService;

    @Autowired
    public AuthService(UserRepository userRepository, AuthDetailsService authDetailsService) {
        this.userRepository = userRepository;
        this.authDetailsService = authDetailsService;
    }

    /**
     * Registers a new user in the system.
     *
     * @param request The user registration request
     * @return StandardResponse containing the registered user
     * @throws ConflictException if email is already registered
     */
    public StandardResponse<User> registerUser(UserRegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException(ApiCode.INVALID_EMAIL_ALREADY_EXIST.getCode(),ApiCode.INVALID_EMAIL_ALREADY_EXIST.getMessage());
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setRole(UserRole.CLIENT);

        User savedUser = userRepository.save(newUser);
        return StandardResponse.success(savedUser);
    }

    /**
     * Finds a user by email.
     *
     * @param email The email to search for
     * @return StandardResponse containing the user if found
     */
    public StandardResponse<User> findByEmail(final String email) {
        return userRepository.findByEmail(email)
                .map(StandardResponse::success)
                .orElse(StandardResponse.error(ApiCode.USER_NOT_FOUND.getCode(), ApiCode.USER_NOT_FOUND.getMessage() + email, HttpStatus.NOT_FOUND));
    }

    /**
     * Attempts to log in a user.
     *
     * @param email The user's email
     * @param password The user's password
     * @return StandardResponse containing the user if credentials are valid
     */
    public StandardResponse<User> loginUser(String email, String password) {
        return findByEmail(email);
    }

    /**
     * Changes the user's password after validating the request.
     *
     * @param changePasswordRequestDto The DTO containing password change details
     * @return StandardResponse indicating the result of the operation
     */
    @Transactional
    public StandardResponse<User> changeUserPassword(PasswordChangeRequest changePasswordRequestDto) {
        return userRepository.findByEmail(changePasswordRequestDto.getEmail())
                .map(user -> StandardResponse.success(user, "Password changed successfully"))
                .orElse(StandardResponse.error(ApiCode.USER_NOT_FOUND.getCode(), ApiCode.USER_NOT_FOUND.getMessage(),HttpStatus.NOT_FOUND));
    }
}