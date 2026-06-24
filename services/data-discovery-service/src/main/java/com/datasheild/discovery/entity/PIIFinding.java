package com.datasheild.discovery.entity;

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
@Table(name = "pii_finding", schema = "discovery", indexes = {
    @Index(name = "idx_scan_tenant", columnList = "scan_id,tenant_id"),
    @Index(name = "idx_pii_type", columnList = "pii_type"),
    @Index(name = "idx_severity", columnList = "severity")
})
public class PIIFinding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID scanId;

    @Column(nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PIIType piiType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String tableName;

    @Column(nullable = false)
    private String columnName;

    @Column(columnDefinition = "TEXT")
    private String matchedValue;

    @Column(columnDefinition = "TEXT")
    private String context;

    private Long recordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetectionMethod detectionMethod;

    @Column(nullable = false)
    private Double confidenceScore;

    @CreationTimestamp
    private LocalDateTime detectedAt;

    public enum PIIType {
        CREDIT_CARD, SSN, PAN, AADHAAR, EMAIL, PHONE, 
        NAME, ADDRESS, DOB, BANK_ACCOUNT, PASSPORT,
        DRIVER_LICENSE, MEDICAL_RECORD, BIOMETRIC, 
        IP_ADDRESS, GPS_LOCATION, CUSTOM
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum DetectionMethod {
        REGEX_PATTERN, ML_MODEL, DICTIONARY_LOOKUP, CUSTOM_RULE
    }
}
