package com.datasheild.configservice.dto;

import com.datasheild.configservice.entity.TenantConfig;
import java.time.LocalDateTime;
import java.util.UUID;

public record TenantConfigResponse(
        UUID id,
        String tenantId,
        String consentModel,
        Integer retentionPeriodDays,
        String dpoEmail,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TenantConfigResponse from(TenantConfig config) {
        return new TenantConfigResponse(
                config.getId(),
                config.getTenantId(),
                config.getConsentModel(),
                config.getRetentionPeriodDays(),
                config.getDpoEmail(),
                config.getStatus().name(),
                config.getCreatedAt(),
                config.getUpdatedAt());
    }
}
