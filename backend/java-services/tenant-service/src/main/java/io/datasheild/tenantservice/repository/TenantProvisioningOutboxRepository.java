package io.datasheild.tenantservice.repository;

import io.datasheild.tenantservice.entity.TenantProvisioningOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TenantProvisioningOutboxRepository extends JpaRepository<TenantProvisioningOutbox, UUID> {

    /**
     * Find unpublished events
     */
    @Query("SELECT o FROM TenantProvisioningOutbox o WHERE o.published = false ORDER BY o.createdAt ASC")
    List<TenantProvisioningOutbox> findUnpublished();

    /**
     * Find unpublished events with retry limit
     */
    @Query("SELECT o FROM TenantProvisioningOutbox o WHERE o.published = false AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<TenantProvisioningOutbox> findRetryable(@Param("maxRetries") Integer maxRetries);

    /**
     * Find events by tenant
     */
    @Query("SELECT o FROM TenantProvisioningOutbox o WHERE o.tenantId = :tenantId ORDER BY o.createdAt DESC")
    List<TenantProvisioningOutbox> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find events by type
     */
    @Query("SELECT o FROM TenantProvisioningOutbox o WHERE o.eventType = :eventType ORDER BY o.createdAt DESC")
    List<TenantProvisioningOutbox> findByEventType(@Param("eventType") String eventType);

    /**
     * Find old unpublished events (for cleanup)
     */
    @Query("SELECT o FROM TenantProvisioningOutbox o WHERE o.published = false AND o.createdAt < :cutoffTime ORDER BY o.createdAt ASC")
    List<TenantProvisioningOutbox> findOldUnpublished(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count unpublished events
     */
    @Query("SELECT COUNT(o) FROM TenantProvisioningOutbox o WHERE o.published = false")
    Long countUnpublished();

    /**
     * Count published events
     */
    @Query("SELECT COUNT(o) FROM TenantProvisioningOutbox o WHERE o.published = true")
    Long countPublished();

    /**
     * Archive (delete) old published events
     */
    @Query("DELETE FROM TenantProvisioningOutbox o WHERE o.published = true AND o.publishedAt < :cutoffTime")
    void archiveOldPublished(@Param("cutoffTime") LocalDateTime cutoffTime);
}
