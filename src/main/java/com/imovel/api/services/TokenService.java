package com.imovel.api.services;

import com.imovel.api.exception.AuthenticationException;
import com.imovel.api.exception.TokenRefreshException;
import com.imovel.api.model.Configuration;
import com.imovel.api.model.RefreshToken;
import com.imovel.api.model.User;
import com.imovel.api.repository.RefreshTokenRepository;
import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.security.token.Token;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenService {

    // Constants
    public static final int CLEANUP_INTERVAL_MS = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    // Dependencies
    private final JWTProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ConfigurationService configurationService;

    @Autowired
    public TokenService(JWTProvider jwtProvider,
                        RefreshTokenRepository refreshTokenRepository,
                        ConfigurationService configurationService) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.configurationService = configurationService;
        this.jwtProvider.initialize();
    }
    /**
     * Authenticates a user and generates new tokens
     *
     * Steps:
     * 1. Enforce token limits by revoking excess tokens
     * 2. Revoke all existing active refresh tokens for the user
     * 3. Generate new access and refresh tokens
     * 4. Save the new refresh token to database
     * 5. Return the token pair
     *
     * @param user The user to authenticate
     * @return Token pair (access + refresh)
     * @throws AuthenticationException if credentials are invalid
     */
    public Token login(User user, HttpServletRequest request) {
        enforceTokenLimits(user.getId());

        Instant now = Instant.now();
        revokeAllUserTokens(user.getId(), now);

        Token tokens = generateTokensForUser(user);
        saveRefreshToken(tokens.getRefreshToken(), user, now,request);

        return tokens;
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
     * 8. Return new token pair
     *
     * @param refreshToken The refresh token string
     * @return New token pair (access + refresh)
     * @throws TokenRefreshException if refresh token is invalid
     */
    public Token refreshToken(String refreshToken, HttpServletRequest request) {
        validateRefreshToken(refreshToken);

        RefreshToken storedToken = getValidRefreshTokenFromDB(refreshToken);
        User user = storedToken.getUser();

        enforceTokenLimits(user.getId());
        supersedePreviousTokens(user.getId(), storedToken.getId());

        Token tokens = generateTokensForUser(user);
        saveRefreshToken(tokens.getRefreshToken(), user, Instant.now(),request);

        return tokens;
    }

    /**
     * Revokes a specific refresh token (logout single device)
     *
     * @param refreshToken The refresh token to revoke
     */
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Revokes all refresh tokens for a user (logout all devices)
     *
     * @param userId The user ID
     */
    public void logoutAll(Long userId) {
        revokeAllUserTokens(userId, Instant.now());
    }

    /**
     * Scheduled task to clean up expired refresh tokens
     * Runs daily based on CLEANUP_INTERVAL_MS
     */
    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
    }

    /**
     * Generates JWT tokens for the given user with appropriate claims
     */
    private Token generateTokensForUser(User user) {
        jwtProvider.addClaim("userId", user.getId().toString());
        jwtProvider.addClaim("username", user.getName());
        jwtProvider.addClaim("role", user.getRole().name());
        return jwtProvider.generateToken();
    }

    /**
     * Saves a new refresh token to the database
     */
    private void saveRefreshToken(String tokenValue, User user, Instant creationTime, HttpServletRequest request) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(creationTime.plusMillis(jwtProvider.getAccessTokenExpirationMs()));
        refreshToken.setIssuedIp(request.getRemoteAddr());
        refreshToken.setUserAgent(request.getHeader("User-Agent"));
        refreshToken.setDeviceFingerprint(generateDeviceFingerprint(request));

        refreshTokenRepository.save(refreshToken);
    }

    private String generateDeviceFingerprint(HttpServletRequest request) {
        // Combine IP, User-Agent, and other immutable characteristics
        return request.getRemoteAddr() +
                        request.getHeader("User-Agent") +
                        request.getHeader("Accept-Language");

    }

    /**
     * Validates a refresh token's JWT signature and expiration
     */
    private void validateRefreshToken(String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new TokenRefreshException("Invalid refresh token");
        }
    }

    /**
     * Retrieves and validates a refresh token from the database
     */
    private RefreshToken getValidRefreshTokenFromDB(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndRevokedFalseAndSupersededFalse(refreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found or invalid"));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenRefreshException("Refresh token expired");
        }

        return storedToken;
    }

    /**
     * Revokes all tokens for a specific user
     */
    private void revokeAllUserTokens(Long userId, Instant revocationTime) {
        refreshTokenRepository.revokeAllUserTokens(userId, revocationTime);
    }

    /**
     * Marks previous tokens as superseded when generating new ones
     */
    private void supersedePreviousTokens(Long userId, Long currentTokenId) {
        refreshTokenRepository.supersedePreviousTokens(userId, currentTokenId);
    }

    /**
     * Revokes a single token
     */
    private void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
    }

    /**
     * Enforces maximum active tokens per user by revoking oldest tokens when limit exceeded
     */
    private void enforceTokenLimits(Long userId) {
        long activeTokenCount = refreshTokenRepository.countActiveTokensByUserId(userId);
        int maxTokens = getMaxRefreshTokensPerUser();

        if (activeTokenCount >= maxTokens) {
            revokeExcessTokens(userId, activeTokenCount - maxTokens + 1);
        }
    }

    /**
     * Retrieves the maximum allowed refresh tokens per user from configuration
     */
    private int getMaxRefreshTokensPerUser() {
        return Integer.valueOf(configurationService
                .findByConfigKey(ConfigurationService.MAX_REFRESH_TOKEN_PER_USER_KEY)
                .get()
                .getConfigValue());
    }

    /**
     * Revokes the oldest tokens for a user when they exceed the limit
     */
    private void revokeExcessTokens(Long userId, long numberOfTokensToRevoke) {
        List<RefreshToken> oldestTokens = refreshTokenRepository
                .findActiveTokensByUserIdOldestFirst(userId)
                .stream()
                .limit(numberOfTokensToRevoke)
                .collect(Collectors.toList());

        oldestTokens.forEach(this::revokeToken);
    }
}