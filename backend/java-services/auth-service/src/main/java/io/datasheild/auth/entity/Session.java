package io.datasheild.auth.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions", schema = "auth",
       indexes = {
           @Index(name = "idx_session_user", columnList = "user_id"),
           @Index(name = "idx_session_tenant", columnList = "tenant_id"),
           @Index(name = "idx_session_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 500)
    private String accessToken;

    @Column(nullable = false, length = 500)
    private String tokenHash;

    @Column(length = 500)
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastActivityAt;

    @Column
    private LocalDateTime revokedAt;

    @Column(length = 500)
    private String revocationReason;

    public enum SessionStatus {
        ACTIVE, REVOKED, EXPIRED, LOCKED
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = SessionStatus.ACTIVE;
        }
        if (lastActivityAt == null) {
            lastActivityAt = LocalDateTime.now();
        }
    }
}
