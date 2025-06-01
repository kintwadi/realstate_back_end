package com.imovel.api.repository;

import com.imovel.api.model.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalseAndSupersededFalse(String token);

    List<RefreshToken> findAllByUserIdAndRevokedFalse(Long userId);

    Optional<RefreshToken> findByUserIdAndRevokedFalseAndSupersededFalse(Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.user.id = :userId AND rt.revoked = false")
    int revokeAllUserTokens(@Param("userId") Long userId, @Param("revokedAt") Instant revokedAt);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.superseded = true " +
            " WHERE rt.user.id = :userId AND rt.superseded = false AND rt.id <> :currentTokenId")
    int supersedePreviousTokens(@Param("userId") Long userId, @Param("currentTokenId") Long currentTokenId);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    int deleteExpiredTokens(@Param("cutoff") Instant cutoff);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false ORDER BY rt.createdAt ASC")
    List<RefreshToken> findActiveTokensByUserIdOldestFirst(@Param("userId") Long userId);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false")
    long countActiveTokensByUserId(@Param("userId") Long userId);
}