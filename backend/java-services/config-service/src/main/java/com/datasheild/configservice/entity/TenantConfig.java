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
@Table(name = "tenant_config", schema = "config", indexes = {
        @Index(name = "idx_tenant_config_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_tenant_config_status", columnList = "status"),
        @Index(name = "idx_tenant_config_created_at", columnList = "created_at")
})
public class TenantConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 64)
    private String tenantId;

    @Column(name = "consent_model", nullable = false, length = 80)
    private String consentModel;

    @Column(name = "retention_period_days", nullable = false)
    private Integer retentionPeriodDays;

    @Column(name = "dpo_email", nullable = false, length = 160)
    private String dpoEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConfigStatus status = ConfigStatus.ACTIVE;
}
