package com.datasheild.configservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feature_flag", schema = "config",
        uniqueConstraints = @UniqueConstraint(name = "uk_feature_flag_tenant_name", columnNames = {"tenant_id", "feature_name"}),
        indexes = {
                @Index(name = "idx_feature_flag_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_feature_flag_status", columnList = "status"),
                @Index(name = "idx_feature_flag_created_at", columnList = "created_at")
        })
public class FeatureFlag extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_name", nullable = false, length = 40)
    private FeatureFlagName featureName;

    @Column(nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConfigStatus status = ConfigStatus.ACTIVE;
}
