package com.datasheild.riskscoring.entity;

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
@Table(name = "risk_score", schema = "risk_scoring", indexes = {
    @Index(name = "idx_risk_tenant_score", columnList = "tenant_id,overall_risk_score DESC"),
    @Index(name = "idx_risk_vendor_scored", columnList = "vendor_id,scored_at DESC")
})
public class RiskScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID vendorId;

    @Column(nullable = false)
    private Double securityFactor;

    @Column(nullable = false)
    private Double complianceFactor;

    @Column(nullable = false)
    private Double operationalFactor;

    @Column(nullable = false)
    private Double historicalFactor;

    @Column(nullable = false)
    private Double overallRiskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TrendDirection trendDirection;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(nullable = false)
    private LocalDateTime scoredAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum TrendDirection {
        FALLING, STABLE, RISING
    }
}
