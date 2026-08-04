package com.datasheild.lineage.entity;

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
@Table(name = "lineage_audit", schema = "lineage")
public class LineageAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID dataFlowId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessType accessType;

    private String accessedBy;

    private String accessReason;

    @Column(columnDefinition = "TEXT")
    private String complianceImpact;

    private Boolean requiresApproval;

    @CreationTimestamp
    private LocalDateTime accessedAt;

    public enum AccessType {
        READ, EXPORT, SHARE_WITH_VENDOR, SHARE_WITH_THIRD_PARTY, PROCESS, DELETE
    }
}
