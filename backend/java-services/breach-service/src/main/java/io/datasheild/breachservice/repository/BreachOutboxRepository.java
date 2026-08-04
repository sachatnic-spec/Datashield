package io.datasheild.breachservice.repository;

import io.datasheild.breachservice.entity.BreachOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BreachOutboxRepository extends JpaRepository<BreachOutbox, UUID> {

    @Query("SELECT o FROM BreachOutbox o WHERE o.tenantId = :tenantId AND o.published = false AND o.retryCount < 5 ORDER BY o.createdAt ASC")
    List<BreachOutbox> findUnpublishedEvents(@Param("tenantId") UUID tenantId);

    @Query("SELECT o FROM BreachOutbox o WHERE o.published = false AND o.retryCount < 5 ORDER BY o.createdAt ASC LIMIT 100")
    List<BreachOutbox> findUnpublishedEventsBatch();

    @Query("SELECT o FROM BreachOutbox o WHERE o.published = true AND o.publishedAt < :before")
    List<BreachOutbox> findPublishedEventsBefore(@Param("before") LocalDateTime before);
}
