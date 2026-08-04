package io.datasheild.auth.repository;

import io.datasheild.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.tenantId = :tenantId AND rt.status = 'VALID' AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidTokenByHashAndTenant(@Param("tokenHash") String tokenHash, @Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.tenantId = :tenantId AND rt.status = 'VALID' AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserAndTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    @Query("UPDATE RefreshToken rt SET rt.status = 'REVOKED', rt.revokedAt = :revokedAt, rt.revocationReason = :reason WHERE rt.userId = :userId AND rt.tenantId = :tenantId")
    void revokeAllUserTokens(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId, @Param("revokedAt") LocalDateTime revokedAt, @Param("reason") String reason);

    @Query("UPDATE RefreshToken rt SET rt.status = 'COMPROMISED' WHERE rt.userId = :userId AND rt.tenantId = :tenantId AND rt.status != 'COMPROMISED'")
    void markTokenFamilyCompromised(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now AND rt.status IN ('VALID', 'USED')")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);
}
