package io.datasheild.analyticsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compliance_metric", schema = "analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceMetric {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String metricType;

    @Column(nullable = false)
    private Double metricValue;

    @Column(length = 50)
    private String unit;

    @Column(nullable = false)
    private LocalDateTime measuredAt;

    @Enumerated(EnumType.STRING)
    private MetricStatus status = MetricStatus.OK;

    @Column(length = 100)
    private String complianceSection;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum MetricStatus {
        OK, WARNING, CRITICAL, BREACH
    }
}
