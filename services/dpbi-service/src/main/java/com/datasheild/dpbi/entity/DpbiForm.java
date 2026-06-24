package com.datasheild.dpbi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dpbi_form", schema = "dpbi", indexes = {
        @Index(name = "idx_dpbi_form_notification_id", columnList = "breach_notification_id"),
        @Index(name = "idx_dpbi_form_generated_at", columnList = "generated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DpbiForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "breach_notification_id", nullable = false)
    private Long breachNotificationId;

    @Column(name = "incident_summary", length = 2000)
    private String incidentSummary;

    @Column(name = "impact_assessment", length = 2000)
    private String impactAssessment;

    @Column(name = "remediation_plan", length = 2000)
    private String remediationPlan;

    @Column(name = "affected_data_subjects")
    private Integer affectedDataSubjects;

    @Column(name = "data_categories", length = 2000)
    private String dataCategories;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    void onCreate() {
        generatedAt = LocalDateTime.now();
        lastUpdatedAt = generatedAt;
    }

    @PreUpdate
    void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}
