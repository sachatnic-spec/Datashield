package com.datasheild.configservice.dto;

import com.datasheild.configservice.entity.TenantConfig;
import java.time.LocalDateTime;

public record ConfigUpdatedEvent(
        String tenantId,
        String consentModel,
        Integer retentionPeriodDays,
        String dpoEmail,
        LocalDateTime updatedAt) {

    public static ConfigUpdatedEvent from(TenantConfig config) {
        return new ConfigUpdatedEvent(
                config.getTenantId(),
                config.getConsentModel(),
                config.getRetentionPeriodDays(),
                config.getDpoEmail(),
                config.getUpdatedAt());
    }
}
