package com.datasheild.webhook.repository;

import com.datasheild.webhook.entity.WebhookRetry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRetryRepository extends JpaRepository<WebhookRetry, Long> {
}
