package io.datasheild.rightsservice.repository;

import io.datasheild.rightsservice.entity.DPROutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DPROutboxRepository extends JpaRepository<DPROutbox, UUID> {

    @Query("SELECT o FROM DPROutbox o WHERE o.tenantId = :tenantId AND o.published = false AND o.retryCount < 5 ORDER BY o.createdAt ASC")
    List<DPROutbox> findUnpublishedEvents(@Param("tenantId") UUID tenantId);

    @Query("SELECT o FROM DPROutbox o WHERE o.published = false AND o.retryCount < 5 ORDER BY o.createdAt ASC LIMIT 100")
    List<DPROutbox> findUnpublishedEventsBatch();

    @Query("SELECT o FROM DPROutbox o WHERE o.published = true AND o.publishedAt < :before")
    List<DPROutbox> findPublishedEventsBefore(@Param("before") LocalDateTime before);
}
