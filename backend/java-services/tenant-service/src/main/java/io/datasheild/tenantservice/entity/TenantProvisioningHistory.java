package io.datasheild.tenantservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_provisioning_history", schema = "tenant", indexes = {
    @Index(name = "idx_provisioning_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_provisioning_status", columnList = "status"),
    @Index(name = "idx_provisioning_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantProvisioningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProvisioningStatus status;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "executed_by", length = 255)
    private String executedBy;

    @Column(name = "duration_ms")
    private Long durationMs;

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

    public enum ProvisioningStatus {
        INITIATED,
        SCHEMA_CREATING,
        TABLES_CREATING,
        INDEXES_CREATING,
        SEED_DATA_LOADING,
        SUCCESS,
        FAILED,
        ROLLED_BACK,
        ARCHIVING,
        ARCHIVED
    }
}
