package com.imovel.api.controller;

import com.imovel.api.error.ApiCode;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.AuthDetails;
import com.imovel.api.model.User;
import com.imovel.api.request.*;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.security.token.Token;
import com.imovel.api.services.AuthDetailsService;
import com.imovel.api.services.AuthService;
import com.imovel.api.services.ForgotPasswordService;
import com.imovel.api.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    private final AuthDetailsService authDetailsService;

    private final ForgotPasswordService forgotPasswordService;
    
    @Autowired
    public AuthController(AuthService authService,
                          JWTProvider jwtProcessor,
                          TokenService tokenService,
                          AuthDetailsService authDetailsService,
                          ForgotPasswordService forgotPasswordService) {
        this.authService = authService;
        this.jwtProvider = jwtProcessor;
        this.tokenService = tokenService;
        this.authDetailsService = authDetailsService;
        this.forgotPasswordService = forgotPasswordService;
        // this.jwtProvider.initialize(); // Commented out - will be initialized by AuthenticationFilter
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
     * Debug endpoint to check user and auth details.
     *
     * @param email User email to check
     * @return Debug information
     */
    @GetMapping("/debug/{email}")
    public ApplicationResponse<String> debugUser(@PathVariable String email) {
        try {
            // Check if user exists
            Optional<User> userOptional = authService.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ApplicationResponse.success("User not found: " + email);
            }
            
            User user = userOptional.get();
            
            // Check if auth details exist
            try {
                ApplicationResponse<AuthDetails> authDetailsResponse = authDetailsService.findByUserId(user.getId());
                String authDetailsInfo = authDetailsResponse.isSuccess() ? 
                    "AuthDetails found" : "AuthDetails not found: " + authDetailsResponse.getMessage();
                
                // Test password verification with the known password
                ApplicationResponse<Boolean> passwordVerification = authService.verifyUserCredentials(user.getId(), "password123");
                String passwordInfo = passwordVerification.isSuccess() ? 
                    "Password verification: " + passwordVerification.getData() : 
                    "Password verification failed: " + passwordVerification.getMessage();
                
                String result = String.format("User found: ID=%d, Email=%s, Name=%s. %s. %s", 
                    user.getId(), user.getEmail(), user.getName(), authDetailsInfo, passwordInfo);
                
                return ApplicationResponse.success(result);
            } catch (Exception authEx) {
                String result = String.format("User found: ID=%d, Email=%s, Name=%s. AuthDetails error: %s", 
                    user.getId(), user.getEmail(), user.getName(), authEx.getMessage());
                return ApplicationResponse.success(result);
            }
        } catch (Exception e) {
            return ApplicationResponse.success("Error: " + e.getMessage());
        }
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
     * Test login endpoint that bypasses aspects for debugging
     */
    @PostMapping("/test-login")
    public ApplicationResponse<String> testLogin(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        try {
            // Test user lookup
            Optional<User> userOpt = authService.findByEmail(loginRequest.getEmail());
            if (!userOpt.isPresent()) {
                return ApplicationResponse.error(404, "User not found", HttpStatus.NOT_FOUND);
            }
            
            User user = userOpt.get();
            
            // Test credential verification
            ApplicationResponse<Boolean> credentialsResponse = authService.verifyUserCredentials(user.getId(), loginRequest.getPassword());
            if (!credentialsResponse.isSuccess() || !Boolean.TRUE.equals(credentialsResponse.getData())) {
                return ApplicationResponse.error(401, "Invalid credentials", HttpStatus.UNAUTHORIZED);
            }
            
            // Test token generation by calling the actual login method
            ApplicationResponse<Token> loginResult = tokenService.login(loginRequest, request);
            if (!loginResult.isSuccess()) {
                return ApplicationResponse.error(500, "Token generation failed: " + 
                    (loginResult.getError() != null ? loginResult.getError().getMessage() : "Unknown error"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            return ApplicationResponse.success("Full login flow successful - tokens generated");
            
        } catch (Exception e) {
            return ApplicationResponse.error(500, "Error: " + e.getMessage() + " - " + e.getClass().getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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