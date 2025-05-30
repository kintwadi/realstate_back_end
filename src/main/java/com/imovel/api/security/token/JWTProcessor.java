package com.imovel.api.security.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.imovel.api.security.keystore.KeyStoreManager;
import com.imovel.api.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles JWT token generation, validation, and claims extraction.
 * Provides secure management of both access and refresh tokens with separate algorithms.
 */
@Component
public final class JWTProcessor {

    private static final String ACCESS_EXPIRATION_MS = "ACCESS_EXPIRATION_MS";
    private static final String REFRESH_EXPIRATION_MS = "REFRESH_EXPIRATION_MS";
    // Configuration fields
    private String issuer;
    private Algorithm accessTokenAlgorithm;
    private Algorithm refreshTokenAlgorithm;
    private long accessTokenExpirationMs;
    private long refreshTokenExpirationMs;
    private Map<String, String> claims;

    @Autowired
    private ConfigurationService configurationService;

    /**
     * Initializes the JWT processor with security configurations and algorithms.
     * Must be called before using other methods.
     */
    public void initialize() {
        claims = new HashMap<>();
        accessTokenExpirationMs = Long.valueOf(configurationService.findByConfigKey(ACCESS_EXPIRATION_MS)
                .get().getConfigValue());
        refreshTokenExpirationMs = Long.valueOf(configurationService.findByConfigKey(REFRESH_EXPIRATION_MS)
                .get().getConfigValue());

        KeyStoreManager keyStoreManager = new KeyStoreManager();
        accessTokenAlgorithm = Algorithm.HMAC256(
                keyStoreManager.retrieveAccessTokenKey()
                        .orElseThrow(() -> new IllegalStateException("Access token key not available"))
                        .getEncoded()
        );
        refreshTokenAlgorithm = Algorithm.HMAC256(
                keyStoreManager.retrieveRefreshTokenKey()
                        .orElseThrow(() -> new IllegalStateException("Refresh token key not available"))
                        .getEncoded()
        );
    }

    /* ========== Token Generation Methods ========== */

    /**
     * Generates a pair of JWT tokens (access and refresh tokens) using configured claims.
     *
     * @return Token object containing both access and refresh tokens
     * @throws IllegalStateException if required configuration is not initialized
     */
    public Token generateToken() {
        if (claims == null) {
            throw new IllegalStateException("JWTProcessor not initialized");
        }

        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        String accessToken = buildAccessToken(claims, now, accessTokenExpirationMs, jti);
        String refreshToken = buildRefreshToken(now, refreshTokenExpirationMs, jti);

        return new Token(accessToken, refreshToken);
    }

    /**
     * Builds an access token with specified claims and expiration.
     *
     * @param claims Map of claims to include
     * @param now Current timestamp
     * @param expirationMs Expiration in milliseconds
     * @param jti Unique JWT ID
     * @return Signed access token
     */
    private String buildAccessToken(Map<String, String> claims, Instant now,
                                    long expirationMs, String jti) {
        Instant expiry = now.plus(expirationMs, ChronoUnit.MILLIS);

        JWTCreator.Builder builder = JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .withJWTId(jti);

        claims.forEach(builder::withClaim);
        return builder.sign(accessTokenAlgorithm);
    }

    /**
     * Builds a refresh token with expiration.
     *
     * @param now Current timestamp
     * @param expirationMs Expiration in milliseconds
     * @param jti Unique JWT ID
     * @return Signed refresh token
     */
    private String buildRefreshToken(Instant now, long expirationMs, String jti) {
        Instant expiry = now.plus(expirationMs, ChronoUnit.MILLIS);

        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .withJWTId(jti)
                .sign(refreshTokenAlgorithm);
    }

    /* ========== Token Validation Methods ========== */

    /**
     * Validates the access token signature and claims.
     *
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token, accessTokenAlgorithm);
    }

    /**
     * Validates the refresh token signature and claims.
     *
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshTokenAlgorithm);
    }

    /**
     * Common token validation logic.
     *
     * @param token Token to validate
     * @param algorithm Algorithm to use for verification
     * @return true if valid, false otherwise
     */
    private boolean validateToken(String token, Algorithm algorithm) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /* ========== Claims Management Methods ========== */

    /**
     * Gets a specific claim from a token.
     *
     * @param name Claim name to retrieve
     * @param token Token to extract from
     * @return Claim value as String
     * @throws NullPointerException if name or token is null
     */
    public String getClaim(String name, String token) {
        Objects.requireNonNull(name, "Claim name cannot be null");
        Objects.requireNonNull(token, "Token cannot be null");

        return JWT.decode(token).getClaim(name).asString();
    }

    /**
     * Gets all claims from a token.
     *
     * @param token Token to extract from
     * @return Map of all claims
     * @throws NullPointerException if token is null
     */
    public Map<String, Claim> getAllClaim(String token) {
        Objects.requireNonNull(token, "Token cannot be null");
        return JWT.decode(token).getClaims();
    }

    /**
     * Adds a claim to be included in generated tokens.
     *
     * @param key Claim key
     * @param value Claim value
     */
    public void addClaim(final String key, final String value) {
        this.claims.putIfAbsent(key, value);
    }

    /* ========== Getters and Setters ========== */

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = Objects.requireNonNull(issuer, "Issuer cannot be null");
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public Map<String, String> getClaims() {
        return claims;
    }
}