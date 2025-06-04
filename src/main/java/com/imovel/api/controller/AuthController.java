package com.imovel.api.controller;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.AuthenticationException;
import com.imovel.api.model.User;
import com.imovel.api.request.*;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.security.token.Token;
import com.imovel.api.services.AuthService;
import com.imovel.api.services.TokenService;
import com.imovel.api.util.Util;
import jakarta.servlet.http.HttpServletRequest;
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
    public AuthController(AuthService authService, JWTProvider jwtProcessor, TokenService tokenService) {
        this.authService = authService;
        this.jwtProcessor = jwtProcessor;
        this.tokenService = tokenService;
        this.jwtProcessor.initialize();
    }

    /**
     * Registers a new user in the system.
     *
     * @param registrationRequest Contains user registration details
     * @return ResponseEntity with registration status
     */
    @PostMapping("/register")
    public ResponseEntity<StandardResponse<User>> registerUser(
            @RequestBody UserRegistrationRequest registrationRequest) {
        StandardResponse<User> response = authService.registerUser(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and generates a JWT token upon successful login.
     *
     * @param loginRequest Contains user credentials
     * @return ResponseEntity with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<StandardResponse<Token>> authenticateUser(
            @RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        StandardResponse<User> userResponse = authService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

        if (!userResponse.isSuccess()) {
            throw new AuthenticationException(ApiCode.INVALID_CREDENTIALS.getCode(), "Invalid email or password");
        }

        StandardResponse<Token>  standardResponse = tokenService.login(userResponse.getData(), request);
        return ResponseEntity.ok(StandardResponse.success(standardResponse.getData()));
    }

    /**
     * Refresh token endpoint.
     *
     * @param refreshTokenRequest Refresh token request
     * @return New token pair (access + refresh)
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<StandardResponse<Token>> refreshToken(
            @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        StandardResponse<Token>  standardResponse = tokenService.refreshToken(refreshTokenRequest.getRefreshToken(), request);
        return ResponseEntity.ok(StandardResponse.success(standardResponse.getData()));
    }

    /**
     * Logout endpoint (single device).
     *
     * @param request Refresh token to revoke
     * @return Success response
     */
    @PostMapping("/logout")
    public ResponseEntity<StandardResponse<Void>> logout(@RequestBody RefreshTokenRequest request) {
        tokenService.logout(request.getRefreshToken());
        return ResponseEntity.ok(StandardResponse.success(null, "Logged out successfully"));
    }

    /**
     * Logout all endpoint (all devices).
     *
     * @param userId The user ID to logout from all devices
     * @return Success response
     */
    @PostMapping("/logout-all")
    public ResponseEntity<StandardResponse<Void>> logoutAll(@RequestParam Long userId) {
        tokenService.logoutAll(userId);
        return ResponseEntity.ok(StandardResponse.success(null, "Logged out from all devices"));
    }

    /**
     * Initiates password reset process.
     *
     * @return StandardResponse indicating initiation status
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<StandardResponse<Void>> initiatePasswordReset() {
        // TODO: Implement password reset initiation
        return ResponseEntity.ok(StandardResponse.success(null, "Password reset initiated"));
    }

    /**
     * Resets user password with new credentials.
     *
     * @param passwordChangeRequest Contains old and new password details
     * @return ResponseEntity with password change status
     */
    @PostMapping("/reset-password")
    public ResponseEntity<StandardResponse<User>> resetUserPassword(
            @RequestBody PasswordChangeRequest passwordChangeRequest) {
        StandardResponse<User> response = authService.changeUserPassword(passwordChangeRequest);
        return ResponseEntity.ok(response);
    }
}