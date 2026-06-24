package io.datasheild.rightsservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dpr_requests", schema = "rights",
       indexes = {
           @Index(name = "idx_dpr_tenant_dp", columnList = "tenant_id,data_principal_id"),
           @Index(name = "idx_dpr_status", columnList = "status"),
           @Index(name = "idx_dpr_type", columnList = "request_type"),
           @Index(name = "idx_dpr_sla_deadline", columnList = "sla_deadline"),
           @Index(name = "idx_dpr_created", columnList = "created_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DPRRequest {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID dataPrincipalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DPRType requestType;  // ACCESS, CORRECTION, ERASURE, PORTABILITY, RESTRICT, OBJECT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DPRStatus status = DPRStatus.RECEIVED;

    @Column(length = 50)
    private String channel;  // WEB, EMAIL, WHATSAPP, PHONE

    @Column(columnDefinition = "JSONB")
    private String requestMetadata;  // JSON: {userAgent, locale, reason, etc}

    @Column(length = 500)
    private String requestDetails;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime slaDeadline;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private UUID verificationChallengeId;

    @Column
    private Boolean identityVerified;

    @Column
    private String rejectionReason;

    @Column
    private Integer activityCount;

    public enum DPRType {
        ACCESS,           // Right to access personal data (§17)
        CORRECTION,       // Right to correction (§18)
        ERASURE,          // Right to erasure (§19)
        PORTABILITY,      // Right to data portability (§20)
        RESTRICT,         // Right to restrict processing (§21)
        OBJECT            // Right to object (§22)
    }

    public enum DPRStatus {
        RECEIVED,         // Initial state
        VERIFICATION_PENDING,  // Awaiting identity verification
        VERIFIED,         // Identity verified
        PROCESSING,       // Being processed
        AWAITING_RESPONSE, // Awaiting internal response
        COMPLETED,        // Completed
        REJECTED,         // Rejected (invalid, duplicate, etc)
        CANCELLED         // Cancelled by DP
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = DPRStatus.RECEIVED;
        }
        if (slaDeadline == null) {
            slaDeadline = LocalDateTime.now().plusDays(30);  // 30-day SLA per DPDP
        }
    }
}
