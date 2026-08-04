package io.datasheild.consentservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_purposes", schema = "consent",
       indexes = {
           @Index(name = "idx_purpose_tenant", columnList = "tenant_id"),
           @Index(name = "idx_purpose_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentPurpose {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String purposeCode;

    @Column(nullable = false, length = 255)
    private String purposeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurposeStatus status = PurposeStatus.ACTIVE;

    @Column(nullable = false)
    private Integer retentionDays;

    @Column(nullable = false)
    @Builder.Default
    private Boolean requiresAudit = true;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime retiredAt;

    public enum PurposeStatus {
        ACTIVE,      // Purpose is active
        RETIRED,     // Purpose no longer used (but consents preserved)
        ARCHIVED     // Historical purpose
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = PurposeStatus.ACTIVE;
        }
    }
}
