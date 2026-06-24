package com.datasheild.aianalysis.entity;

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
@Table(name = "anomaly_detection", schema = "ai_analysis", indexes = {
    @Index(name = "idx_tenant_metric", columnList = "tenant_id,metric_type"),
    @Index(name = "idx_detected_at", columnList = "detected_at DESC")
})
public class AnomalyDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;

    @Column(nullable = false)
    private Double metricValue;

    @Column(nullable = false)
    private Double baselineValue;

    @Column(nullable = false)
    private Double deviationPercentage;

    @Column(nullable = false)
    private Double anomalyScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnomalySeverity severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetectionMethod detectionMethod;

    @Column(nullable = false)
    private Boolean isAlertTriggered;

    @CreationTimestamp
    private LocalDateTime detectedAt;

    public enum MetricType {
        GRIEVANCE_SLA_BREACH_RATE, DSAR_PROCESSING_TIME, CONSENT_WITHDRAWAL_RATE,
        BREACH_INCIDENT_COUNT, VENDOR_RISK_ESCALATION, DATA_ACCESS_ANOMALY,
        RETENTION_FAILURE_RATE, NOTIFICATION_DELAY
    }

    public enum AnomalySeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum DetectionMethod {
        STANDARD_DEVIATION, ISOLATION_FOREST, MOVING_AVERAGE, THRESHOLD_BASED
    }
}
