package io.datasheild.auditservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "audit_events",
    schema = "audit",
    indexes = {
        @Index(name = "idx_audit_tenant", columnList = "tenant_id"),
        @Index(name = "idx_audit_created", columnList = "created_at"),
        @Index(name = "idx_audit_source", columnList = "source_service"),
        @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
        @Index(name = "idx_audit_event_type", columnList = "event_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String correlationId;

    @Column(nullable = false)
    private String sourceService;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private UUID entityId;

    @Column(columnDefinition = "TEXT")
    private String eventPayload;

    @Column(nullable = false)
    private String actorId;

    @Column
    private String actorRole;

    @Column
    private String ipAddress;

    @Column
    private String userAgent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private String previousState;

    @Column
    private String currentState;

    public boolean isValid() {
        return tenantId != null && correlationId != null && !correlationId.isBlank() &&
               sourceService != null && !sourceService.isBlank() &&
               entityType != null && !entityType.isBlank() &&
               eventType != null && !eventType.isBlank() &&
               entityId != null && actorId != null && !actorId.isBlank();
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
