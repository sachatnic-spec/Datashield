package io.datasheild.auth.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mfa_devices", schema = "auth",
       indexes = {
           @Index(name = "idx_mfa_user", columnList = "user_id"),
           @Index(name = "idx_mfa_tenant", columnList = "tenant_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MFADevice {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MFAType type;

    @Column(nullable = false, length = 255)
    private String deviceIdentifier;

    @Column(length = 500)
    private String secret;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private Integer failedAttempts;

    @Column
    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum MFAType {
        TOTP,      // Time-based One-Time Password (Google Authenticator)
        SMS,       // SMS-based OTP
        EMAIL,     // Email-based OTP
        WEBAUTHN   // WebAuthn/FIDO2
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (failedAttempts == null) {
            failedAttempts = 0;
        }
    }
}
