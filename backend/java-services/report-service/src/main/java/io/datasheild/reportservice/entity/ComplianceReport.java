package io.datasheild.reportservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compliance_report", schema = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceReport {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String reportType;

    @Column(nullable = false, length = 255)
    private String reportTitle;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.SCHEDULED;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    private LocalDateTime completedAt;

    @Column(length = 500)
    private String fileLocation;

    @Column(length = 50)
    private String fileFormat;

    @Enumerated(EnumType.STRING)
    private ReportFrequency frequency;

    private Double complianceScore;

    @Column(length = 1000)
    private String summary;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ReportStatus {
        SCHEDULED, GENERATING, COMPLETED, FAILED, ARCHIVED
    }

    public enum ReportFrequency {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY
    }
}
