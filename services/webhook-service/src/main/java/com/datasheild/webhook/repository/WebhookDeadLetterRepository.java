package com.datasheild.webhook.repository;

import com.datasheild.webhook.entity.WebhookDeadLetter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookDeadLetterRepository extends JpaRepository<WebhookDeadLetter, Long> {
}
