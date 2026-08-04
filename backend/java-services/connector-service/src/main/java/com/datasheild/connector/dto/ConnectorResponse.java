package com.datasheild.connector.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ConnectorResponse(
        Long id,
        String tenantId,
        String name,
        String connectorType,
        String sourceType,
        String targetType,
        String endpoint,
        String status,
        LocalDateTime lastSyncedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
