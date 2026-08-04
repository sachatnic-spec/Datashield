package io.datasheild.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "dead_letter_queue",
    schema = "notification",
    indexes = {
        @Index(name = "idx_dlq_status", columnList = "status"),
        @Index(name = "idx_dlq_created", columnList = "created_at"),
        @Index(name = "idx_dlq_reason", columnList = "failure_reason")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeadLetterQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(columnDefinition = "TEXT")
    private String originalPayload;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column
    private Integer retryCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DLQStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime resolvedAt;

    public enum DLQStatus {
        PENDING,
        REVIEWING,
        RESOLVED,
        DISCARDED
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = DLQStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
