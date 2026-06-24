package io.datasheild.consentservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_notices", schema = "consent",
       indexes = {
           @Index(name = "idx_notice_tenant", columnList = "tenant_id"),
           @Index(name = "idx_notice_version", columnList = "version_number")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentNotice {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false, length = 50)
    private String languageCode;  // en, hi, te, ta, etc (22 languages)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String noticeContent;

    @Column(columnDefinition = "TEXT")
    private String privacyPolicyUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NoticeStatus status = NoticeStatus.ACTIVE;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deprecatedAt;

    @Column(nullable = false)
    private Long dataRetentionDays;

    public enum NoticeStatus {
        DRAFT,       // Notice in preparation
        ACTIVE,      // Current active notice
        DEPRECATED   // Old version, only for historical reference
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = NoticeStatus.ACTIVE;
        }
    }
}
