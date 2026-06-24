package io.datasheild.vendorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendors", schema = "vendor", indexes = {
    @Index(name = "idx_vendor_name", columnList = "name"),
    @Index(name = "idx_vendor_type", columnList = "vendor_type"),
    @Index(name = "idx_vendor_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "vendor_type", nullable = false, length = 100)
    private String vendorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VendorStatus status;

    @Column(name = "website", length = 512)
    private String website;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "data_processor_role", nullable = false, length = 100)
    private String dataProcessorRole;

    @Column(name = "country_of_operation", length = 100)
    private String countryOfOperation;

    @Column(name = "data_categories", columnDefinition = "TEXT")
    private String dataCategories;

    @Column(name = "processing_purposes", columnDefinition = "TEXT")
    private String processingPurposes;

    @Column(name = "data_retention_policy", columnDefinition = "TEXT")
    private String dataRetentionPolicy;

    @Column(name = "has_dpa", nullable = false)
    private Boolean hasDPA = false;

    @Column(name = "dpa_signed_date")
    private LocalDateTime dpaSignedDate;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore = 50;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "risk_last_assessed")
    private LocalDateTime riskLastAssessed;

    @Column(name = "audit_notes", columnDefinition = "TEXT")
    private String auditNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (riskLevel == null) {
            riskLevel = RiskLevel.MEDIUM;
        }
    }

    public enum VendorStatus {
        PROSPECT,
        ONBOARDING,
        ACTIVE,
        SUSPENDED,
        OFFBOARDED
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
