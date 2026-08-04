package io.datasheild.consentservice.repository;

import io.datasheild.consentservice.entity.ConsentAuditOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsentAuditOutboxRepository extends JpaRepository<ConsentAuditOutbox, UUID> {

    @Query("SELECT o FROM ConsentAuditOutbox o WHERE o.tenantId = :tenantId AND o.published = false AND o.retryCount < 5 ORDER BY o.createdAt ASC")
    List<ConsentAuditOutbox> findUnpublishedEvents(@Param("tenantId") UUID tenantId);

    @Query("SELECT o FROM ConsentAuditOutbox o WHERE o.published = false AND o.retryCount < 5 ORDER BY o.createdAt ASC LIMIT 100")
    List<ConsentAuditOutbox> findUnpublishedEventsBatch();

    @Query("SELECT COUNT(o) FROM ConsentAuditOutbox o WHERE o.tenantId = :tenantId AND o.published = true AND o.publishedAt >= :since")
    long countPublishedEventsSince(@Param("tenantId") UUID tenantId, @Param("since") LocalDateTime since);

    @Query("SELECT o FROM ConsentAuditOutbox o WHERE o.published = true AND o.publishedAt < :before")
    List<ConsentAuditOutbox> findPublishedEventsBefore(@Param("before") LocalDateTime before);
}
