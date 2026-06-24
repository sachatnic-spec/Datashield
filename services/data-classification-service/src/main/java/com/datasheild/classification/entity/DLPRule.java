package com.datasheild.classification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dlp_rule", schema = "classification")
public class DLPRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataClassification.SensitivityLevel appliesTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleStatus status;

    private Integer priority;

    @Column(nullable = false)
    private Boolean enabled;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum RuleType {
        PREVENT_COPY, PREVENT_PRINT, PREVENT_FORWARD, 
        REQUIRE_APPROVAL, MASK_ON_VIEW, ENCRYPT_AT_REST,
        WATERMARK, AUDIT_LOG, RESTRICT_ACCESS
    }

    public enum Action {
        ALLOW, BLOCK, REQUIRE_APPROVAL, ALERT, REDACT
    }

    public enum RuleStatus {
        DRAFT, ACTIVE, INACTIVE, ARCHIVED
    }
}
