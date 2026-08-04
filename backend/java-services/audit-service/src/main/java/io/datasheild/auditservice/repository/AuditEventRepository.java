package io.datasheild.auditservice.repository;

import io.datasheild.auditservice.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.tenantId = :tenantId AND ae.createdAt >= :since ORDER BY ae.createdAt DESC")
    Page<AuditEvent> findByTenantSince(@Param("tenantId") UUID tenantId, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.tenantId = :tenantId AND ae.sourceService = :sourceService AND ae.createdAt >= :since ORDER BY ae.createdAt DESC")
    Page<AuditEvent> findBySourceService(@Param("tenantId") UUID tenantId, @Param("sourceService") String sourceService, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.tenantId = :tenantId AND ae.entityType = :entityType AND ae.entityId = :entityId ORDER BY ae.createdAt DESC")
    List<AuditEvent> findByEntity(@Param("tenantId") UUID tenantId, @Param("entityType") String entityType, @Param("entityId") UUID entityId);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.tenantId = :tenantId AND ae.eventType = :eventType AND ae.createdAt >= :since ORDER BY ae.createdAt DESC")
    Page<AuditEvent> findByEventType(@Param("tenantId") UUID tenantId, @Param("eventType") String eventType, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.tenantId = :tenantId AND ae.actorId = :actorId AND ae.createdAt >= :since ORDER BY ae.createdAt DESC")
    Page<AuditEvent> findByActor(@Param("tenantId") UUID tenantId, @Param("actorId") String actorId, @Param("since") LocalDateTime since, Pageable pageable);
}
