package com.datasheild.searchservice.repository;

import com.datasheild.searchservice.entity.AuditLogIndex;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogIndexRepository extends JpaRepository<AuditLogIndex, UUID> {

    @Query("select coalesce(avg(a.complianceScore), 0) from AuditLogIndex a where a.tenantId = :tenantId and a.complianceScore is not null")
    BigDecimal averageComplianceScore(@Param("tenantId") String tenantId);

    @Query("select coalesce(min(a.complianceScore), 0), coalesce(max(a.complianceScore), 0) from AuditLogIndex a where a.tenantId = :tenantId and a.complianceScore is not null")
    List<Object[]> complianceScoreRange(@Param("tenantId") String tenantId);

    @Query("select a.eventType, count(a) from AuditLogIndex a where a.tenantId = :tenantId group by a.eventType")
    List<Object[]> countByEventType(@Param("tenantId") String tenantId);

    long countByTenantId(String tenantId);

    long deleteByCreatedAtBefore(Instant cutoff);
}
