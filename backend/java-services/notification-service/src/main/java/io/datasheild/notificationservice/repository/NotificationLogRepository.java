package io.datasheild.notificationservice.repository;

import io.datasheild.notificationservice.entity.NotificationLog;
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
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.eventId = :eventId AND nl.tenantId = :tenantId ORDER BY nl.createdAt DESC")
    List<NotificationLog> findByEvent(@Param("eventId") UUID eventId, @Param("tenantId") UUID tenantId);

    @Query("SELECT nl FROM NotificationLog nl WHERE nl.tenantId = :tenantId AND nl.deliveryStatus = :status AND nl.createdAt >= :since ORDER BY nl.createdAt DESC")
    Page<NotificationLog> findByStatus(@Param("tenantId") UUID tenantId, @Param("status") NotificationLog.DeliveryStatus status, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT COUNT(nl) FROM NotificationLog nl WHERE nl.eventId = :eventId AND nl.deliveryStatus = 'DELIVERED'")
    long countSuccessfulDeliveries(@Param("eventId") UUID eventId);
}
