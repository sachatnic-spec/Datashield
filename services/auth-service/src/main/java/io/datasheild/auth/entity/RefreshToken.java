package io.datasheild.auth.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", schema = "auth",
       indexes = {
           @Index(name = "idx_refresh_user", columnList = "user_id"),
           @Index(name = "idx_refresh_tenant", columnList = "tenant_id"),
           @Index(name = "idx_refresh_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 500)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TokenStatus status = TokenStatus.VALID;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime revokedAt;

    @Column(length = 500)
    private String revocationReason;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    public enum TokenStatus {
        VALID,        // Token is valid and can be used
        USED,         // Token was rotated (new token issued)
        REVOKED,      // Token was explicitly revoked
        EXPIRED,      // Token has expired
        COMPROMISED   // Token family invalidated (theft signal)
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = TokenStatus.VALID;
        }
    }
}
