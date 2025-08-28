package com.imovel.api.controller;

import com.imovel.api.model.User;
import com.imovel.api.request.*;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.security.token.Token;
import com.imovel.api.services.AuthService;
import com.imovel.api.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final JWTProvider jwtProvider;
    private final TokenService tokenService;

    @Autowired
    public AuthController(AuthService authService,
                          JWTProvider jwtProcessor,
                          TokenService tokenService) {
        this.authService = authService;
        this.jwtProvider = jwtProcessor;
        this.tokenService = tokenService;
        this.jwtProvider.initialize();
    }

    /**
     * Registers a new user in the system.
     *
     * @param registrationRequest Contains user registration details
     * @return ApplicationResponse with registration status
     */
    @PostMapping("/register")
    public ApplicationResponse<UserResponse> registerUser(
            @RequestBody UserRegistrationRequest registrationRequest) {
        return authService.registerUser(registrationRequest);
    }

    /**
     * Authenticates a user and generates a JWT token upon successful login.
     *
     * @param loginRequest Contains user credentials
     * @return ApplicationResponse with JWT token
     */
    @PostMapping("/login")
    public ApplicationResponse<Token> authenticateUser(
            @RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        return tokenService.login(loginRequest, request);
    }

    /**
     * Refresh token endpoint.
     *
     * @param refreshTokenRequest Refresh token request
     * @return New token pair (access + refresh)
     */
    @PostMapping("/refresh-token")
    public ApplicationResponse<Token> refreshToken(
            @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        ApplicationResponse<Token> standardResponse = tokenService.refreshToken(refreshTokenRequest.getRefreshToken(), request);
        return ApplicationResponse.success(standardResponse.getData());
    }

    /**
     * Logout endpoint (single device).
     *
     * @param request Refresh token to revoke
     * @return Success response
     */
    @PostMapping("/logout")
    public ApplicationResponse<Void> logout(@RequestBody RefreshTokenRequest request) {
        tokenService.logout(request.getRefreshToken());
        return ApplicationResponse.success(null, "Logged out successfully");
    }

    /**
     * Logout all endpoint (all devices).
     *
     * @param userId The user ID to logout from all devices
     * @return Success response
     */
    @PostMapping("/logout-all")
    public ApplicationResponse<Void> logoutAll(@RequestParam Long userId) {
        tokenService.logoutAll(userId);
        return ApplicationResponse.success(null, "Logged out from all devices");
    }

    /**
     * Resets user password with new credentials.
     *
     * @param passwordChangeRequest Contains old and new password details
     * @return ApplicationResponse with password change status
     */
    @PostMapping("/reset-password")
    public ApplicationResponse<UserResponse> initiatePasswordReset(
            @RequestBody PasswordChangeRequest passwordChangeRequest) {
        return authService.changeUserPassword(passwordChangeRequest);
    }
}