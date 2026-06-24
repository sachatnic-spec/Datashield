package io.datasheild.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "notification_templates",
    schema = "notification",
    indexes = {
        @Index(name = "idx_template_code", columnList = "template_code"),
        @Index(name = "idx_template_event_type", columnList = "event_type"),
        @Index(name = "idx_template_tenant", columnList = "tenant_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum EventType {
        CONSENT_GRANTED,
        CONSENT_WITHDRAWN,
        CONSENT_EXPIRED,
        DPR_SUBMITTED,
        DPR_VERIFIED,
        DPR_COMPLETED,
        BREACH_REPORTED,
        BREACH_CONTAINED,
        BREACH_NOTIFIED_DPBI,
        BREACH_NOTIFIED_DP
    }

    public enum TemplateStatus {
        ACTIVE,
        INACTIVE,
        ARCHIVED
    }

    public boolean isValid() {
        return tenantId != null && templateCode != null && !templateCode.isBlank() &&
               eventType != null && subject != null && !subject.isBlank() &&
               body != null && !body.isBlank();
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = TemplateStatus.ACTIVE;
        }
    }
}
