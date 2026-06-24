package io.datasheild.notificationservice.repository;

import io.datasheild.notificationservice.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    @Query("SELECT t FROM NotificationTemplate t WHERE t.tenantId = :tenantId AND t.templateCode = :code AND t.status = 'ACTIVE'")
    Optional<NotificationTemplate> findActiveByTenantAndCode(@Param("tenantId") UUID tenantId, @Param("code") String code);

    @Query("SELECT t FROM NotificationTemplate t WHERE t.tenantId = :tenantId AND t.eventType = :eventType AND t.status = 'ACTIVE'")
    List<NotificationTemplate> findByEventType(@Param("tenantId") UUID tenantId, @Param("eventType") NotificationTemplate.EventType eventType);

    @Query("SELECT t FROM NotificationTemplate t WHERE t.tenantId = :tenantId AND t.status = 'ACTIVE' ORDER BY t.createdAt DESC")
    List<NotificationTemplate> findAllActive(@Param("tenantId") UUID tenantId);
}
