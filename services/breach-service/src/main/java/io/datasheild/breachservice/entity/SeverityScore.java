package io.datasheild.breachservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "severity_scores", schema = "breach",
       indexes = {
           @Index(name = "idx_severity_breach", columnList = "breach_incident_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeverityScore {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID breachIncidentId;

    @Column(nullable = false)
    private Integer affectedDataSubjects;

    @Column(nullable = false)
    private Integer affectedRecords;

    @Column(nullable = false)
    private Boolean sensitiveDataInvolved;

    @Column(nullable = false)
    private Boolean highRiskDataSubjects;  // Children, elderly, etc

    @Column(nullable = false)
    private Integer baseScore;  // 0-100 base calculation

    @Column(nullable = false)
    private Integer adjustedScore;  // After adjustments

    @Column
    private String scoringRationale;  // Explain the score

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BreachIncident.SeverityLevel proposedSeverity;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime calculatedAt;

    @Column
    private LocalDateTime reviewedAt;

    @Column
    private String reviewedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
