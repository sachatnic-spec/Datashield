package io.datasheild.notificationservice.repository;

import io.datasheild.notificationservice.entity.NotificationEvent;
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
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.tenantId = :tenantId AND ne.status = 'PENDING' ORDER BY ne.createdAt ASC LIMIT 100")
    List<NotificationEvent> findPendingEventsBatch(@Param("tenantId") UUID tenantId);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.tenantId = :tenantId AND ne.status = 'FAILED' AND ne.retryCount < 5 ORDER BY ne.createdAt ASC")
    List<NotificationEvent> findFailedEventsForRetry(@Param("tenantId") UUID tenantId);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.tenantId = :tenantId AND ne.status = :status AND ne.createdAt >= :since ORDER BY ne.createdAt DESC")
    Page<NotificationEvent> findByStatus(@Param("tenantId") UUID tenantId, @Param("status") NotificationEvent.EventStatus status, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.correlationId = :correlationId AND ne.tenantId = :tenantId")
    List<NotificationEvent> findByCorrelation(@Param("correlationId") String correlationId, @Param("tenantId") UUID tenantId);
}
