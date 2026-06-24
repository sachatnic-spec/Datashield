package com.datasheild.piidetection.entity;

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
@Table(name = "pii_detection_result", schema = "pii_detection", indexes = {
    @Index(name = "idx_tenant_confidence", columnList = "tenant_id,confidence_score DESC"),
    @Index(name = "idx_pii_type", columnList = "pii_type")
})
public class PIIDetectionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(columnDefinition = "TEXT")
    private String inputText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PIICategory piiType;

    @Column(nullable = false)
    private Double confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String context;

    @Column(columnDefinition = "TEXT")
    private String redactedText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetectionSource detectionSource;

    @Column(nullable = false)
    private Boolean requiresHumanReview;

    @CreationTimestamp
    private LocalDateTime detectedAt;

    public enum PIICategory {
        CREDIT_CARD, SSN, AADHAAR, PAN, EMAIL, PHONE,
        NAME, ADDRESS, DOB, BANK_ACCOUNT, PASSPORT,
        DRIVER_LICENSE, MEDICAL, BIOMETRIC, LOCATION,
        CUSTOM, UNKNOWN
    }

    public enum DetectionSource {
        REGEX_PATTERN, ML_MODEL, HYBRID, MANUAL
    }
}
