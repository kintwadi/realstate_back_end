package com.imovel.api.controller;

import com.imovel.api.model.User;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.security.token.JWTProcessor;
import com.imovel.api.services.AuthService;
import com.imovel.api.util.Util;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling authentication-related operations including
 * user registration, login, logout, and password management.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JWTProcessor jwtProcessor;

    @Autowired
    public AuthController(AuthService authService, JWTProcessor jwtProcessor) {
        this.authService = authService;
        this.jwtProcessor = jwtProcessor;
    }

    /**
     * Registers a new user in the system
     * @param registrationRequest Contains user registration details
     * @return ResponseEntity with registration status
     */
    @PostMapping("/register")
    public ResponseEntity<StandardResponse<Object>> registerUser(
            @RequestBody UserRegistrationRequest registrationRequest) {

        return authService.registerUser(registrationRequest)
                .map(user -> new ResponseEntity<>(
                        new StandardResponse<>("User registered successfully", null, null),
                        HttpStatus.CREATED))
                .orElse(new ResponseEntity<>(
                        new StandardResponse<>("Email already registered", "REGISTRATION_002", null),
                        HttpStatus.CONFLICT));
    }

    /**
     * Authenticates a user and generates a JWT token upon successful login
     * @param loginRequest Contains user credentials
     * @return ResponseEntity with JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<StandardResponse<String>> authenticateUser(
            @RequestBody UserLoginRequest loginRequest) {

        return authService.loginUser(loginRequest.getEmail(), loginRequest.getPassword())
                .map(user -> {
                    prepareJwtToken(loginRequest, user);
                    return new ResponseEntity<>(
                            new StandardResponse<>("Login successful", "LOGIN_001",
                                    Util.toJSON(jwtProcessor.generateToken())),
                            HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(
                        new StandardResponse<>("Invalid email or password", "LOGIN_002", null),
                        HttpStatus.UNAUTHORIZED));
    }

    /**
     * Prepares JWT token with user claims
     * @param loginRequest User login details
     * @param user Authenticated user entity
     */
    private void prepareJwtToken(UserLoginRequest loginRequest, User user) {
        jwtProcessor.initialize();
        jwtProcessor.setIssuer("api.auth.login");
        jwtProcessor.addClaim("userId", String.valueOf(user.getId()));
        jwtProcessor.addClaim("email", loginRequest.getEmail());
    }

    /**
     * Handles user logout (currently not implemented)
     * @return Empty standard response
     */
    @PostMapping("/logout")
    public StandardResponse<Void> logoutUser() {
        // TODO: Implement logout logic (token invalidation, session cleanup)
        return new StandardResponse<>();
    }

    /**
     * Initiates password reset process (currently not implemented)
     * @return Empty standard response
     */
    @PostMapping("/forgot-password")
    public StandardResponse<Void> initiatePasswordReset() {
        // TODO: Implement password reset initiation (email verification, token generation)
        return new StandardResponse<>();
    }

    /**
     * Resets user password with new credentials
     * @param passwordChangeRequest Contains old and new password details
     * @param session HTTP session
     * @return ResponseEntity with password change status
     */
    @PostMapping("/reset-password")
    public ResponseEntity<StandardResponse<Object>> resetUserPassword(
            @RequestBody PasswordChangeRequest passwordChangeRequest,
            HttpSession session) {

        StandardResponse response = authService.changeUserPassword(passwordChangeRequest);
        return new ResponseEntity<>(
                new StandardResponse<>(response.getErrorText(),
                        response.getErrorCode(), null),
                HttpStatus.OK);
    }
}