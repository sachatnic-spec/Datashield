package com.datasheild.webhook.repository;

import com.datasheild.webhook.entity.WebhookEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, Long> {
    Page<WebhookEndpoint> findByTenantId(String tenantId, Pageable pageable);
    List<WebhookEndpoint> findByTenantIdAndIsActiveTrue(String tenantId);
    Optional<WebhookEndpoint> findByIdAndTenantId(Long id, String tenantId);
}
