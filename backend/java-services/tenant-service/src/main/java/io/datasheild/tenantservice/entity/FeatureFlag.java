package io.datasheild.tenantservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feature_flags", schema = "tenant", indexes = {
    @Index(name = "idx_feature_flag_name", columnList = "flag_name"),
    @Index(name = "idx_feature_flag_tier", columnList = "tier"),
    @Index(name = "idx_feature_flag_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "flag_name", nullable = false, length = 255)
    private String flagName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private Tenant.TenantTier tier;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "api_quota_per_month")
    private Long apiQuotaPerMonth;

    @Column(name = "concurrent_requests_limit")
    private Integer concurrentRequestsLimit;

    @Column(name = "feature_value", columnDefinition = "TEXT")
    private String featureValue;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public static FeatureFlag createForTier(Tenant.TenantTier tier, String flagName, Boolean active) {
        return FeatureFlag.builder()
            .flagName(flagName)
            .tier(tier)
            .isActive(active)
            .build();
    }
}
