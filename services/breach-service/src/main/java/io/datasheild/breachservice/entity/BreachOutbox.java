package io.datasheild.breachservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "breach_outbox", schema = "breach",
       indexes = {
           @Index(name = "idx_breach_outbox_tenant", columnList = "tenant_id"),
           @Index(name = "idx_breach_outbox_status", columnList = "published")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreachOutbox {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 50)
    private String eventType;  // breach.reported, breach.contained, breach.notified

    @Column(columnDefinition = "JSONB", nullable = false)
    private String eventPayload;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    @Column
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private Integer retryCount;

    @Column
    private String lastError;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (published == null) {
            published = false;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
