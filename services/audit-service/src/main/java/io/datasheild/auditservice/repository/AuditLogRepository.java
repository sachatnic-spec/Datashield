package io.datasheild.auditservice.repository;

import io.datasheild.auditservice.entity.AuditLog;
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
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT al FROM AuditLog al WHERE al.tenantId = :tenantId AND al.createdAt >= :since AND al.archived = false ORDER BY al.createdAt DESC")
    Page<AuditLog> findActive(@Param("tenantId") UUID tenantId, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.tenantId = :tenantId AND al.archived = true AND al.archivedAt >= :since ORDER BY al.archivedAt DESC")
    Page<AuditLog> findArchived(@Param("tenantId") UUID tenantId, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.auditEventId = :eventId AND al.tenantId = :tenantId")
    AuditLog findByEventId(@Param("eventId") UUID eventId, @Param("tenantId") UUID tenantId);

    @Query("SELECT al FROM AuditLog al WHERE al.tenantId = :tenantId AND al.archived = false AND al.createdAt < :cutoffDate ORDER BY al.createdAt LIMIT 1000")
    List<AuditLog> findForArchival(@Param("tenantId") UUID tenantId, @Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.tenantId = :tenantId AND al.archived = false")
    long countActive(@Param("tenantId") UUID tenantId);
}
