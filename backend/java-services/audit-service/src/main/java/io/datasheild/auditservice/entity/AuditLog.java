package io.datasheild.auditservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "audit_logs",
    schema = "audit",
    indexes = {
        @Index(name = "idx_log_audit_event", columnList = "audit_event_id"),
        @Index(name = "idx_log_tenant", columnList = "tenant_id"),
        @Index(name = "idx_log_created", columnList = "created_at"),
        @Index(name = "idx_log_archived", columnList = "archived")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID auditEventId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventSummary;

    @Column
    private String s3ObjectKey;

    @Column
    private Long fileSizeBytes;

    @Column
    private String sha256Hash;

    @Column
    private String previousEventHash;

    @Column
    private String hashChainValid;

    @Column
    private Boolean archived;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime archivedAt;

    public boolean isValid() {
        return auditEventId != null && tenantId != null &&
               eventSummary != null && !eventSummary.isBlank();
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (archived == null) {
            archived = false;
        }
    }
}
