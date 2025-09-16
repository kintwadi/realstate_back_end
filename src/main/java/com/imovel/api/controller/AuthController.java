package com.imovel.api.controller;

import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.User;
import com.imovel.api.request.*;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.security.token.Token;
import com.imovel.api.services.AuthService;
import com.imovel.api.services.ForgotPasswordService;
import com.imovel.api.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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

    private final ForgotPasswordService forgotPasswordService;

    @Autowired
    public AuthController(AuthService authService,
                          JWTProvider jwtProcessor,
                          TokenService tokenService,
                          ForgotPasswordService forgotPasswordService) {
        this.authService = authService;
        this.jwtProvider = jwtProcessor;
        this.tokenService = tokenService;
        this.forgotPasswordService = forgotPasswordService;
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

    @PostMapping("/forgot-password")
    public ApplicationResponse<Void> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        return forgotPasswordService.requestReset(request);
    }

    @PostMapping("/reset-password")
    public ApplicationResponse<Void> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        return forgotPasswordService.reset(request);
    }

    @PostMapping("/change-password")
    public ApplicationResponse<Void> changePassword(@RequestBody ChangePasswordRequest request,
                                                    HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        if (userId == null) {
            ApiLogger.error(buildLogTag("changePassword"), "No valid userId in JWT");
            return ApplicationResponse.error(
                    ApiCode.AUTHENTICATION_FAILED.getCode(),
                    ApiCode.AUTHENTICATION_FAILED.getMessage(),
                    ApiCode.AUTHENTICATION_FAILED.getHttpStatus()
            );
        }
        return authService.changePassword(request, userId);
    }

    private String buildLogTag(String method) {
        return "AuthController." + method;
    }

    // Your provided helper, moved to controller:
    private Long getUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // The "userId" claim is added in TokenService.generateTokensForUser
                String userIdStr = jwtProvider.getClaim("userId", token);
                return Long.parseLong(userIdStr);
            } catch (Exception e) {
                ApiLogger.error(buildLogTag("getUserIdFromToken"),
                        "Could not extract userId from token.", e);
                return null;
            }
        }
        return null;
    }

}