package io.datasheild.policyservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "policies", schema = "policy", indexes = {
    @Index(name = "idx_policy_name", columnList = "name"),
    @Index(name = "idx_policy_status", columnList = "status"),
    @Index(name = "idx_policy_category", columnList = "category"),
    @Index(name = "idx_policy_version", columnList = "policy_version")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PolicyStatus status;

    @Column(name = "policy_version", nullable = false)
    private Integer policyVersion = 1;

    @Column(name = "dpdp_section", length = 50)
    private String dpdpSection;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "enforcement_level", nullable = false, length = 50)
    private String enforcementLevel;

    @Column(name = "applicable_tiers", length = 255)
    private String applicableTiers;

    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;

    @Column(name = "approved_by", length = 255)
    private String approvedBy;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "deprecated_date")
    private LocalDateTime deprecatedDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = PolicyStatus.DRAFT;
        }
    }

    public enum PolicyStatus {
        DRAFT,
        PENDING_REVIEW,
        APPROVED,
        ACTIVE,
        DEPRECATED,
        ARCHIVED
    }
}
