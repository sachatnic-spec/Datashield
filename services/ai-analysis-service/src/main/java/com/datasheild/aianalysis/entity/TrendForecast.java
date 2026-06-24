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
@Table(name = "trend_forecast", schema = "ai_analysis")
public class TrendForecast {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;

    @Column(nullable = false)
    private Integer forecastDays;

    @Column(nullable = false)
    private Double currentValue;

    @Column(nullable = false)
    private Double predictedValue;

    @Column(nullable = false)
    private Double confidenceInterval;

    @Column(columnDefinition = "TEXT")
    private String forecast30DayTrend;

    @Column(nullable = false)
    private Double trendDirection;

    @CreationTimestamp
    private LocalDateTime generatedAt;

    public enum MetricType {
        GRIEVANCE_VOLUME, DSAR_REQUESTS, BREACH_RISK, CONSENT_ADOPTION,
        DATA_ACCESS_PATTERNS, VENDOR_RISK_TREND, RETENTION_FAILURES
    }
}
