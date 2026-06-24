package io.datasheild.consentservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_records", schema = "consent",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"tenant_id", "data_principal_id", "purpose_id", "status"})
       },
       indexes = {
           @Index(name = "idx_consent_tenant_dp", columnList = "tenant_id,data_principal_id"),
           @Index(name = "idx_consent_purpose", columnList = "purpose_id,status"),
           @Index(name = "idx_consent_status", columnList = "status"),
           @Index(name = "idx_consent_expires", columnList = "expires_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRecord {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID dataPrincipalId;

    @Column(nullable = false)
    private UUID purposeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsentStatus status = ConsentStatus.GRANTED;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String deviceFingerprint;

    @Column(length = 50)
    private String channel;  // WEB, MOBILE, EMAIL, WHATSAPP

    @Column(columnDefinition = "TEXT")
    private String metadata;  // JSON: {userAgent, locale, etc}

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime grantedAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime lastModifiedAt;

    @Column
    private LocalDateTime withdrawnAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(length = 500)
    private String withdrawalReason;

    @Column(nullable = false)
    @Builder.Default
    private Boolean auditLogged = false;

    public enum ConsentStatus {
        GRANTED,    // Consent is active
        WITHDRAWN,  // Consent withdrawn by data principal
        EXPIRED,    // Consent expired after retention period
        REVOKED     // Consent revoked by DPO/compliance
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = ConsentStatus.GRANTED;
        }
    }
}
