package io.datasheild.vendorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments", schema = "vendor", indexes = {
    @Index(name = "idx_risk_vendor_id", columnList = "vendor_id"),
    @Index(name = "idx_risk_level", columnList = "risk_level")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "vendor_id", nullable = false, columnDefinition = "uuid")
    private UUID vendorId;

    @Column(name = "assessment_type", nullable = false, length = 100)
    private String assessmentType;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private Vendor.RiskLevel riskLevel;

    @Column(name = "security_score")
    private Integer securityScore;

    @Column(name = "compliance_score")
    private Integer complianceScore;

    @Column(name = "operational_score")
    private Integer operationalScore;

    @Column(name = "findings_summary", columnDefinition = "TEXT")
    private String findingsSummary;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "assessment_date")
    private LocalDateTime assessmentDate;

    @Column(name = "assessed_by", length = 255)
    private String assessedBy;

    @Column(name = "next_assessment_date")
    private LocalDateTime nextAssessmentDate;

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
    }
}
