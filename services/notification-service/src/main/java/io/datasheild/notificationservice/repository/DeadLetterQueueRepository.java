package io.datasheild.notificationservice.repository;

import io.datasheild.notificationservice.entity.DeadLetterQueue;
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
public interface DeadLetterQueueRepository extends JpaRepository<DeadLetterQueue, UUID> {

    @Query("SELECT dlq FROM DeadLetterQueue dlq WHERE dlq.tenantId = :tenantId AND dlq.status = 'PENDING' ORDER BY dlq.createdAt ASC")
    List<DeadLetterQueue> findPendingDLQs(@Param("tenantId") UUID tenantId);

    @Query("SELECT dlq FROM DeadLetterQueue dlq WHERE dlq.tenantId = :tenantId AND dlq.status = :status AND dlq.createdAt >= :since ORDER BY dlq.createdAt DESC")
    Page<DeadLetterQueue> findByStatus(@Param("tenantId") UUID tenantId, @Param("status") DeadLetterQueue.DLQStatus status, @Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT dlq FROM DeadLetterQueue dlq WHERE dlq.eventId = :eventId AND dlq.tenantId = :tenantId")
    DeadLetterQueue findByEventId(@Param("eventId") UUID eventId, @Param("tenantId") UUID tenantId);
}
