package io.datasheild.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "notification_logs",
    schema = "notification",
    indexes = {
        @Index(name = "idx_log_event", columnList = "event_id"),
        @Index(name = "idx_log_channel", columnList = "channel"),
        @Index(name = "idx_log_status", columnList = "delivery_status"),
        @Index(name = "idx_log_created", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column
    private String externalReference;

    @Column
    private Integer attemptNumber;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Channel {
        EMAIL,
        SMS,
        WHATSAPP,
        PUSH,
        INAPP,
        WEBHOOK
    }

    public enum DeliveryStatus {
        SENT,
        DELIVERED,
        FAILED,
        BOUNCED,
        DEFERRED
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (attemptNumber == null) {
            attemptNumber = 1;
        }
    }
}
