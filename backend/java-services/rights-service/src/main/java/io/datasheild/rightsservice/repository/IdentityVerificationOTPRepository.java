package io.datasheild.rightsservice.repository;

import io.datasheild.rightsservice.entity.IdentityVerificationOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentityVerificationOTPRepository extends JpaRepository<IdentityVerificationOTP, UUID> {

    @Query("SELECT o FROM IdentityVerificationOTP o WHERE o.tenantId = :tenantId AND o.dprRequestId = :requestId AND o.status = 'SENT' AND o.expiresAt > :now")
    Optional<IdentityVerificationOTP> findActivePending(@Param("tenantId") UUID tenantId, @Param("requestId") UUID requestId, @Param("now") LocalDateTime now);

    @Query("SELECT o FROM IdentityVerificationOTP o WHERE o.tenantId = :tenantId AND o.dprRequestId = :requestId ORDER BY o.createdAt DESC LIMIT 1")
    Optional<IdentityVerificationOTP> findLatestByRequest(@Param("tenantId") UUID tenantId, @Param("requestId") UUID requestId);

    @Query("SELECT o FROM IdentityVerificationOTP o WHERE o.dprRequestId = :requestId AND o.status = 'VERIFIED'")
    Optional<IdentityVerificationOTP> findVerifiedOTP(@Param("requestId") UUID requestId);
}
