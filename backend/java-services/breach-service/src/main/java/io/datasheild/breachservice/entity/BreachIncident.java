package io.datasheild.breachservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "breach_incidents", schema = "breach",
       indexes = {
           @Index(name = "idx_breach_tenant", columnList = "tenant_id"),
           @Index(name = "idx_breach_status", columnList = "status"),
           @Index(name = "idx_breach_severity", columnList = "severity"),
           @Index(name = "idx_breach_dpbi_deadline", columnList = "dpbi_deadline"),
           @Index(name = "idx_breach_created", columnList = "created_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreachIncident {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BreachStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel severity;

    @Column(length = 500, nullable = false)
    private String incidentTitle;

    @Column(columnDefinition = "TEXT")
    private String incidentDescription;

    @Column(length = 200)
    private String affectedSystems;  // Comma-separated or JSON

    @Column
    private Integer estimatedDataSubjects;  // Count of affected individuals

    @Column
    private Integer estimatedRecords;  // Count of affected records

    @Column(columnDefinition = "JSONB")
    private String dataCategories;  // JSON: ["personal_info", "financial", "health", ...]

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime discoveredAt;

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    @Column
    private LocalDateTime dpbiNotifiedAt;

    @Column
    private LocalDateTime dpNotifiedAt;

    @Column
    private LocalDateTime processorNotifiedAt;

    @Column
    private LocalDateTime containedAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private LocalDateTime dpbiDeadline;  // 72 hours from discovery

    @Column(length = 500)
    private String rootCause;

    @Column
    private Boolean lossOfConfidentiality;

    @Column
    private Boolean lossOfIntegrity;

    @Column
    private Boolean lossOfAvailability;

    @Column
    private String containmentStrategy;

    @Column
    private UUID dpbiFormId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum BreachStatus {
        REPORTED,          // Initial report
        INVESTIGATING,     // Under investigation
        CONTAINED,         // Contained
        NOTIFIED_DPBI,     // DPBI notified
        NOTIFIED_DP,       // Data principals notified
        RESOLVED,          // Resolved
        CLOSED             // Closed
    }

    public enum SeverityLevel {
        P0,  // Critical (affects > 10k individuals or sensitive data)
        P1,  // High (affects 1k-10k individuals)
        P2,  // Medium (affects 100-1k individuals)
        P3   // Low (affects < 100 individuals)
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (reportedAt == null) {
            reportedAt = LocalDateTime.now();
        }
        if (dpbiDeadline == null) {
            dpbiDeadline = discoveredAt.plusHours(72);  // 72-hour deadline per DPDP
        }
    }
}
