package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.AuthenticationException;
import com.imovel.api.exception.TokenRefreshException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.RefreshToken;

import com.imovel.api.model.User;
import com.imovel.api.repository.RefreshTokenRepository;
import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.security.token.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TokenService {

    // Constants
    public static final int CLEANUP_INTERVAL_MS = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    // Dependencies
    private final JWTProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ConfigurationService configurationService;
    private final AuthService authService;
    private boolean jwtInitialized = false;

    @Autowired
    public TokenService(JWTProvider jwtProvider,
                        RefreshTokenRepository refreshTokenRepository,
                        ConfigurationService configurationService, 
                        AuthService authService) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.configurationService = configurationService;
        this.authService = authService;
        // JWT initialization is now handled lazily to avoid startup timing issues
    }

    /**
     * Lazy initialization of JWT components to ensure Spring context is fully loaded
     */
    private void ensureJwtInitialized() {
        if (!jwtInitialized) {
            synchronized (this) {
                if (!jwtInitialized) {
                    try {
                        configurationService.setDefaultConfigurations();
                        jwtProvider.initialize();
                        jwtInitialized = true;
                    } catch (Exception e) {
                        ApiLogger.error("TokenService", "Failed to initialize JWT components: " + e.getMessage(), e);
                        throw new RuntimeException("JWT initialization failed", e);
                    }
                }
            }
        }
    }

    /**
     * Authenticates a user and generates new tokens
     *
     * Steps:
     * 1. Enforce token limits by revoking excess tokens
     * 2. Revoke all existing active refresh tokens for the user
     * 3. Generate new access and refresh tokens
     * 4. Save the new refresh token to database
     * 5. Return the token pair wrapped in StandardResponse
     *
     * @param loginRequest The user to authenticate
     * @param request HTTP request for device information
     * @return StandardResponse containing Token pair (access + refresh)
     * @throws AuthenticationException if credentials are invalid (HTTP 401)
     */
    public ApplicationResponse<Token> login(UserLoginRequest loginRequest, HttpServletRequest request) {


        ApiLogger.info("TokenService.login: initialization");
        try {
            ApiLogger.debug("TokenService.login", "Looking up user by email", Map.of("email", loginRequest.getEmail()));
            Optional<User> optionalUser = authService.findByEmail(loginRequest.getEmail());
            if(!optionalUser.isPresent()){
               ApiLogger.debug("TokenService.login", "User not found", Map.of("email", loginRequest.getEmail()));
               return  ApplicationResponse.error(ApiCode.AUTHENTICATION_FAILED.getCode(), ApiCode.AUTHENTICATION_FAILED.getMessage(), ApiCode.AUTHENTICATION_FAILED.getHttpStatus());
            }
            ApiLogger.debug("TokenService.login", "User found", Map.of("userId", optionalUser.get().getId()));
            
            // enforce password check...
            ApiLogger.debug("TokenService.login", "About to verify password for user", Map.of("userId", optionalUser.get().getId()));
            ApplicationResponse<Boolean> verificationResult = authService.verifyUserCredentials(optionalUser.get().getId(), loginRequest.getPassword());
            ApiLogger.debug("TokenService.login", "Password verification result", Map.of("isSuccess", verificationResult.isSuccess(), "data", verificationResult.getData()));
            
            if(!verificationResult.isSuccess() || !Boolean.TRUE.equals(verificationResult.getData())){
                ApiLogger.debug("TokenService.login", "Password verification failed", Map.of("isSuccess", verificationResult.isSuccess(), "data", verificationResult.getData(), "error", verificationResult.getError()));
                return  ApplicationResponse.error(ApiCode.INVALID_CREDENTIALS.getCode(), ApiCode.INVALID_CREDENTIALS.getMessage(), ApiCode.INVALID_CREDENTIALS.getHttpStatus());
            }
            ApiLogger.debug("TokenService.login", "Password verification successful");
            
            enforceTokenLimits(optionalUser.get().getId());
            ApiLogger.debug("TokenService.login", "Token limits enforced");

            Instant now = Instant.now();
            revokeAllUserTokens(optionalUser.get().getId(), now);
            ApiLogger.debug("TokenService.login", "Previous tokens revoked");

            Token tokens = generateTokensForUser(optionalUser.get());
            ApiLogger.debug("TokenService.login", "Tokens generated successfully");
            
            saveRefreshToken(tokens.getRefreshToken(), optionalUser.get(), now, request);
            ApiLogger.debug("TokenService.login", "Refresh token saved");

            return ApplicationResponse.success(tokens);
        } catch (Exception ex) {
            ApiLogger.error("TokenService.login", "Login failed with exception", 
                Map.of("exception", ex.getClass().getSimpleName(), "message", ex.getMessage()));
            ex.printStackTrace();
            return ApplicationResponse.error(ApiCode.AUTHENTICATION_FAILED.getCode(), ApiCode.AUTHENTICATION_FAILED.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Refreshes an access token using a valid refresh token
     *
     * Steps:
     * 1. Validate the refresh token (JWT signature and expiration)
     * 2. Verify the token exists in database and isn't revoked/superseded
     * 3. Get the associated user
     * 4. Enforce token limits
     * 5. Mark previous refresh token as superseded
     * 6. Generate new token pair
     * 7. Save new refresh token
     * 8. Return new token pair wrapped in StandardResponse
     *
     * @param refreshToken The refresh token string
     * @param request HTTP request for device information
     * @return StandardResponse containing new Token pair (access + refresh)
     * @throws TokenRefreshException if refresh token is invalid (HTTP 401)
     */
    public ApplicationResponse<Token> refreshToken(String refreshToken, HttpServletRequest request) {
        validateRefreshToken(refreshToken);

        RefreshToken storedToken = getValidRefreshTokenFromDB(refreshToken);
        User user = storedToken.getUser();

        enforceTokenLimits(user.getId());
        supersedePreviousTokens(user.getId(), storedToken.getId());

        Token tokens = generateTokensForUser(user);
        saveRefreshToken(tokens.getRefreshToken(), user, Instant.now(), request);

        return ApplicationResponse.success(tokens);
    }

    /**
     * Revokes a specific refresh token (logout single device)
     *
     * @param refreshToken The refresh token to revoke
     * @return StandardResponse indicating success or failure
     */
    public ApplicationResponse<Void> logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });

        return ApplicationResponse.success(null);
    }

    /**
     * Revokes all refresh tokens for a user (logout all devices)
     *
     * @param userId The user ID
     * @return StandardResponse indicating success or failure
     */
    public ApplicationResponse<Void> logoutAll(Long userId) {
        revokeAllUserTokens(userId, Instant.now());
        return ApplicationResponse.success(null);
    }

    /**
     * Scheduled task to clean up expired refresh tokens
     * Runs daily based on CLEANUP_INTERVAL_MS
     */
    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
    }

    // ============ PRIVATE HELPER METHODS ============ //

    /**
     * Generates JWT tokens for the given user with appropriate claims
     * @param user The user to generate tokens for
     * @return Generated token pair
     */
    private Token generateTokensForUser(User user) {
        ensureJwtInitialized();
        ApiLogger.debug("TokenService.generateTokensForUser", "Generating tokens for user", 
            Map.of("userId", user.getId(), "username", user.getName()));
        
        String roleName = "TENANT"; // Default role for users without assigned roles
        if (user.getRole() == null) {
            ApiLogger.warn("TokenService.generateTokensForUser", "User has no role assigned, using default TENANT role for token generation", 
                Map.of("userId", user.getId()));
        } else {
            roleName = user.getRole().getRoleName();
        }
        
        jwtProvider.addClaim("userId", user.getId().toString());
        jwtProvider.addClaim("username", user.getName());
        jwtProvider.addClaim("role", roleName);
        ApiLogger.debug("TokenService.generateTokensForUser", "Added claims to JWT", 
            Map.of("userId", user.getId(), "role", roleName));
        return jwtProvider.generateToken();
    }

    /**
     * Saves a new refresh token to the database
     * @param tokenValue The token value to save
     * @param user The associated user
     * @param creationTime Token creation time
     * @param request HTTP request for device information
     */
    private void saveRefreshToken(String tokenValue, User user, Instant creationTime, HttpServletRequest request) {
        ensureJwtInitialized();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(creationTime.plusMillis(jwtProvider.getAccessTokenExpirationMs()));
        refreshToken.setIssuedIp(request.getRemoteAddr());
        refreshToken.setUserAgent(request.getHeader("User-Agent"));
        refreshToken.setDeviceFingerprint(generateDeviceFingerprint(request));

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Generates a device fingerprint from request characteristics
     * @param request HTTP request
     * @return Generated fingerprint string
     */
    private String generateDeviceFingerprint(HttpServletRequest request) {
        return request.getRemoteAddr() +
                request.getHeader("User-Agent") +
                request.getHeader("Accept-Language");
    }

    /**
     * Validates a refresh token's JWT signature and expiration
     * @param refreshToken The token to validate
     * @throws TokenRefreshException if token is invalid
     */
    private void validateRefreshToken(String refreshToken) {
        ensureJwtInitialized();
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new TokenRefreshException(ApiCode.INVALID_REFRESH_TOKEN.getCode(),
                    ApiCode.INVALID_REFRESH_TOKEN.getMessage(),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Retrieves and validates a refresh token from the database
     * @param refreshToken The token to retrieve
     * @return Valid RefreshToken entity
     * @throws TokenRefreshException if token is invalid or expired
     */
    private RefreshToken getValidRefreshTokenFromDB(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndSupersededFalse(refreshToken)
                .orElseThrow(() -> new TokenRefreshException(ApiCode.REFRESH_TOKEN_NOT_FOUND.getCode(),
                        ApiCode.REFRESH_TOKEN_NOT_FOUND.getMessage(),
                        HttpStatus.UNAUTHORIZED));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenRefreshException(ApiCode.REFRESH_TOKEN_EXPIRED.getCode(),
                    ApiCode.REFRESH_TOKEN_EXPIRED.getMessage(),
                    HttpStatus.UNAUTHORIZED);
        }

        return storedToken;
    }

    /**
     * Revokes all tokens for a specific user
     * @param userId The user ID
     * @param revocationTime Time of revocation
     */
    private void revokeAllUserTokens(Long userId, Instant revocationTime) {
        refreshTokenRepository.revokeAllUserTokens(userId, revocationTime);
    }

    /**
     * Marks previous tokens as superseded when generating new ones
     * @param userId The user ID
     * @param currentTokenId The ID of the current valid token
     */
    private void supersedePreviousTokens(Long userId, Long currentTokenId) {
        refreshTokenRepository.supersedePreviousTokens(userId, currentTokenId);
    }

    /**
     * Revokes a single token
     * @param token The token to revoke
     */
    private void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
    }

    /**
     * Enforces maximum active tokens per user by revoking oldest tokens when limit exceeded
     * @param userId The user ID to check
     * @throws TokenRefreshException if token limit is exceeded
     */
    private void enforceTokenLimits(Long userId) {
        long activeTokenCount = refreshTokenRepository.countActiveTokensByUserId(userId);
        int maxTokens = getMaxRefreshTokensPerUser();

        if (activeTokenCount >= maxTokens) {
            revokeExcessTokens(userId, activeTokenCount - maxTokens + 1);
            throw new TokenRefreshException(ApiCode.REFRESH_TOKEN_NOT_LIMITE_EXCEEDED.getCode(),
                    ApiCode.REFRESH_TOKEN_NOT_LIMITE_EXCEEDED.getMessage(),
                    HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    /**
     * Retrieves the maximum allowed refresh tokens per user from configuration
     * @return Maximum allowed tokens
     */
    private int getMaxRefreshTokensPerUser() {
        return Integer.parseInt(configurationService
                .findByConfigKey(ConfigurationService.MAX_REFRESH_TOKEN_PER_USER_KEY)
                .orElseThrow(() -> new IllegalStateException("Token limit configuration not found"))
                .getConfigValue());
    }
    /**
     * Revokes the oldest tokens for a user when they exceed the limit
     * @param userId The user ID
     * @param numberOfTokensToRevoke Number of tokens to revoke
     */
    private void revokeExcessTokens(Long userId, long numberOfTokensToRevoke) {
        List<RefreshToken> oldestTokens = refreshTokenRepository
                .findActiveTokensByUserIdOldestFirst(userId)
                .stream()
                .limit(numberOfTokensToRevoke)
                .collect(Collectors.toList());

        oldestTokens.forEach(this::revokeToken);
    }

    public String getClaim(final String name, String token){
        ensureJwtInitialized();
        return jwtProvider.getClaim(name,token);
    }

}