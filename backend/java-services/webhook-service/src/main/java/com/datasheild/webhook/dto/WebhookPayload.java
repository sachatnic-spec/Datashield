package com.datasheild.webhook.dto;

import lombok.Builder;

@Builder
public record WebhookPayload(
        String eventType,
        String payload,
        String tenantId
) {
}
