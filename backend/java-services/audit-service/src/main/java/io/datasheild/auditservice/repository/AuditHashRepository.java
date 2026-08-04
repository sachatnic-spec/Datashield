package io.datasheild.auditservice.repository;

import io.datasheild.auditservice.entity.AuditHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditHashRepository extends JpaRepository<AuditHash, UUID> {

    @Query("SELECT ah FROM AuditHash ah WHERE ah.auditLogId = :logId AND ah.tenantId = :tenantId ORDER BY ah.sequenceNumber DESC LIMIT 1")
    Optional<AuditHash> findLatestByLog(@Param("logId") UUID logId, @Param("tenantId") UUID tenantId);

    @Query("SELECT ah FROM AuditHash ah WHERE ah.tenantId = :tenantId AND ah.validChain = false")
    List<AuditHash> findInvalidChains(@Param("tenantId") UUID tenantId);

    @Query("SELECT ah FROM AuditHash ah WHERE ah.auditLogId = :logId AND ah.tenantId = :tenantId ORDER BY ah.sequenceNumber ASC")
    List<AuditHash> findChainForLog(@Param("logId") UUID logId, @Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(ah) FROM AuditHash ah WHERE ah.tenantId = :tenantId")
    long countByTenant(@Param("tenantId") UUID tenantId);
}
