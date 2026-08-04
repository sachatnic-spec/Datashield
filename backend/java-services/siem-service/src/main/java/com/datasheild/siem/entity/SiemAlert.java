package com.datasheild.siem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "siem_alert", schema = "siem", indexes = {
        @Index(name = "idx_siem_alert_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_siem_alert_severity", columnList = "severity"),
        @Index(name = "idx_siem_alert_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiemAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(nullable = false)
    private String severity;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "external_incident_id")
    private String externalIncidentId;

    @Column(length = 2000)
    private String message;

    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "NEW";
        }
    }
}
