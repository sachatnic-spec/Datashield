package io.datasheild.tenantservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "tenant", indexes = {
    @Index(name = "idx_tenant_name", columnList = "name"),
    @Index(name = "idx_tenant_status", columnList = "status"),
    @Index(name = "idx_tenant_tier", columnList = "tier"),
    @Index(name = "idx_tenant_schema", columnList = "schema_name", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private TenantTier tier;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    private SubscriptionStatus subscriptionStatus;

    @Column(name = "schema_name", nullable = false, unique = true, length = 63)
    private String schemaName;

    @Enumerated(EnumType.STRING)
    @Column(name = "provisioning_status", nullable = false)
    private ProvisioningStatus provisioningStatus;

    @Column(name = "max_data_principals", nullable = false)
    private Long maxDataPrincipals;

    @Column(name = "max_consents", nullable = false)
    private Long maxConsents;

    @Column(name = "max_dpr_requests", nullable = false)
    private Long maxDPRRequests;

    @Column(name = "max_storage_gb", nullable = false)
    private Long maxStorageGB;

    @Column(name = "api_rate_limit_rpm", nullable = false)
    private Integer apiRateLimitRPM;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Column(name = "support_email", length = 255)
    private String supportEmail;

    @Column(name = "support_phone", length = 20)
    private String supportPhone;

    @Column(name = "contract_start_date")
    private LocalDateTime contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDateTime contractEndDate;

    @Column(name = "auto_renewal", nullable = false)
    private Boolean autoRenewal = true;

    @Column(name = "invoice_email", length = 255)
    private String invoiceEmail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (provisioningStatus == null) {
            provisioningStatus = ProvisioningStatus.PENDING;
        }
        if (subscriptionStatus == null) {
            subscriptionStatus = SubscriptionStatus.ACTIVE;
        }
    }

    public enum TenantTier {
        STARTER,
        PROFESSIONAL,
        ENTERPRISE,
        GOVERNMENT
    }

    public enum SubscriptionStatus {
        TRIAL,
        ACTIVE,
        SUSPENDED,
        CANCELLED,
        ARCHIVED
    }

    public enum ProvisioningStatus {
        PENDING,
        CREATING,
        ACTIVE,
        ARCHIVING,
        ARCHIVED,
        FAILED
    }
}
