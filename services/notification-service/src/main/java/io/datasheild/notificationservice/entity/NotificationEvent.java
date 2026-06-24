package io.datasheild.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "notification_events",
    schema = "notification",
    indexes = {
        @Index(name = "idx_event_status", columnList = "status"),
        @Index(name = "idx_event_created", columnList = "created_at"),
        @Index(name = "idx_event_tenant", columnList = "tenant_id"),
        @Index(name = "idx_event_type", columnList = "event_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column
    private String recipientEmail;

    @Column
    private String recipientPhone;

    @Column
    private Integer retryCount;

    @Column
    private LocalDateTime scheduledFor;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum EventType {
        NOTIFICATION_TRIGGERED,
        NOTIFICATION_SENT,
        NOTIFICATION_FAILED,
        NOTIFICATION_BOUNCED
    }

    public enum EventStatus {
        PENDING,
        PROCESSING,
        SENT,
        FAILED,
        BOUNCED,
        ARCHIVED
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (status == null) {
            status = EventStatus.PENDING;
        }
    }
}
