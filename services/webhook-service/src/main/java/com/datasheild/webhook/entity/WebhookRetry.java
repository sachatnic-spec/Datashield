package com.datasheild.webhook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_retry", schema = "webhook", indexes = {
        @Index(name = "idx_webhook_retry_event_id", columnList = "event_id"),
        @Index(name = "idx_webhook_retry_next_attempt", columnList = "next_attempt_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookRetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @PrePersist
    void onCreate() {
        if (status == null) {
            status = "SCHEDULED";
        }
    }
}
