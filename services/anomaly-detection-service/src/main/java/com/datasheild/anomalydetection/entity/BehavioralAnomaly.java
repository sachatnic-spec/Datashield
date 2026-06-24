package com.datasheild.anomalydetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
@Table(name = "behavioral_anomaly", schema = "anomaly", indexes = {
    @Index(name = "idx_behavior_user_detected", columnList = "user_id,detected_at DESC"),
    @Index(name = "idx_behavior_risk", columnList = "overall_risk_score DESC")
})
public class BehavioralAnomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime accessTime;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer volume;

    @Column(nullable = false)
    private Double timeDeviationScore;

    @Column(nullable = false)
    private Double volumeDeviationScore;

    @Column(nullable = false)
    private Double geographyDeviationScore;

    @Column(nullable = false)
    private Double overallRiskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Severity severity;

    @Column(nullable = false)
    private Boolean unauthorizedAccess;

    @Column(nullable = false)
    private Boolean criticalAlert;

    @Column(nullable = false)
    private Integer baselineWindowDays;

    @Column(columnDefinition = "TEXT")
    private String profileSnapshot;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @CreationTimestamp
    private LocalDateTime detectedAt;

    public enum Severity {
        LOW, MODERATE, HIGH, CRITICAL
    }
}
