package com.imovel.api.controller;

import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.RefreshTokenRequest;
import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.security.token.Token;
import com.imovel.api.services.AuthService;
import com.imovel.api.services.TokenService;
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
    private final JWTProvider jwtProcessor;
    private final TokenService tokenService;

    @Autowired
    public AuthController(AuthService authService, JWTProvider jwtProcessor,TokenService tokenService) {
        this.authService = authService;
        this.jwtProcessor = jwtProcessor;
        this.tokenService = tokenService;
        this.jwtProcessor.initialize();
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
                    Token tokens = tokenService.login(user);
                    return new ResponseEntity<>(
                            new StandardResponse<>("Login successful", "LOGIN_001",
                                    Util.toJSON(tokens)),
                            HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(
                        new StandardResponse<>("Invalid email or password", "LOGIN_002", null),
                        HttpStatus.UNAUTHORIZED));
    }
    /**
     * Refresh token endpoint
     *
     * Steps:
     * 1. Receive refresh token
     * 2. Delegate to AuthService for token refresh
     * 3. Return new token pair
     *
     * @param request Refresh token request
     * @return New token pair (access + refresh)
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Token> refreshToken(@RequestBody RefreshTokenRequest request) {
        Token tokens = tokenService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }

    /**
     * Logout endpoint (single device)
     *
     * @param request Refresh token to revoke
     * @return Success response
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        tokenService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * Logout all endpoint (all devices)
     *
     * @param userId The user ID to logout from all devices
     * @return Success response
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestParam Long userId) {
        tokenService.logoutAll(userId);
        return ResponseEntity.ok().build();
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