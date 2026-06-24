package io.datasheild.tenantservice.dto;

import io.datasheild.tenantservice.entity.FeatureFlag;
import io.datasheild.tenantservice.entity.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureFlagResponse {

    private UUID id;

    private String flagName;

    private String description;

    private Tenant.TenantTier tier;

    private Boolean isActive;

    private Long apiQuotaPerMonth;

    private Integer concurrentRequestsLimit;

    private String featureValue;

    private String metadata;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static FeatureFlagResponse fromEntity(FeatureFlag flag) {
        return FeatureFlagResponse.builder()
            .id(flag.getId())
            .flagName(flag.getFlagName())
            .description(flag.getDescription())
            .tier(flag.getTier())
            .isActive(flag.getIsActive())
            .apiQuotaPerMonth(flag.getApiQuotaPerMonth())
            .concurrentRequestsLimit(flag.getConcurrentRequestsLimit())
            .featureValue(flag.getFeatureValue())
            .metadata(flag.getMetadata())
            .createdAt(flag.getCreatedAt())
            .updatedAt(flag.getUpdatedAt())
            .build();
    }
}
