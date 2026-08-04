package com.datasheild.webhook.repository;

import com.datasheild.webhook.entity.WebhookEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Page<WebhookEvent> findByTenantId(String tenantId, Pageable pageable);
    List<WebhookEvent> findTop20ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(String status, LocalDateTime now);
}
