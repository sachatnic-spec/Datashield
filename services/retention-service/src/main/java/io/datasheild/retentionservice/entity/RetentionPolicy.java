package io.datasheild.retentionservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "retention_policy", schema = "retention")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetentionPolicy {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 255)
    private String policyName;

    @Column(length = 50)
    private String sector;

    @Column(length = 100)
    private String dataCategory;

    @Column(nullable = false)
    private Integer retentionDaysDefault;

    @Column(nullable = false)
    private Integer retentionDaysMax;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status = PolicyStatus.DRAFT;

    @Column(length = 1000)
    private String disposalMethod;

    @Column(nullable = false)
    private Boolean requiresApproval = true;

    @Column(length = 255)
    private String approvedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PolicyStatus {
        DRAFT, ACTIVE, ARCHIVED, SUSPENDED
    }
}
