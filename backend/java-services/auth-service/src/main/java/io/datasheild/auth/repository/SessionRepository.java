package io.datasheild.auth.repository;

import io.datasheild.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Query("SELECT s FROM Session s WHERE s.userId = :userId AND s.tenantId = :tenantId AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    List<Session> findActiveSessionsByUserAndTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Session s WHERE s.accessToken = :token AND s.tenantId = :tenantId AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    Optional<Session> findByAccessTokenAndTenantId(@Param("token") String token, @Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Session s WHERE s.refreshToken = :token AND s.tenantId = :tenantId AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    Optional<Session> findByRefreshTokenAndTenantId(@Param("token") String token, @Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.userId = :userId AND s.tenantId = :tenantId AND s.status = 'ACTIVE'")
    long countActiveSessionsByUserAndTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    @Query("UPDATE Session s SET s.status = 'REVOKED', s.revokedAt = :revokedAt WHERE s.userId = :userId AND s.tenantId = :tenantId")
    void revokeAllUserSessions(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId, @Param("revokedAt") LocalDateTime revokedAt);

    @Query("SELECT s FROM Session s WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    List<Session> findExpiredActiveSessions(@Param("now") LocalDateTime now);
}
