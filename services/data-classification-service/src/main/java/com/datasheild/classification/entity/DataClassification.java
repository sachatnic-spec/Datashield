package com.datasheild.classification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data_classification", schema = "classification")
public class DataClassification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String dataSetName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensitivityLevel sensitivityLevel;

    @Column(nullable = false)
    private String tableName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer recordCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassificationStatus status;

    @Column(nullable = false)
    private Integer piiFieldCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataOwnershipType ownershipType;

    private String owner;

    private Boolean dlpEnforced;

    @CreationTimestamp
    private LocalDateTime classifiedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum SensitivityLevel {
        PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED
    }

    public enum ClassificationStatus {
        UNCLASSIFIED, CLASSIFIED, REVIEW_PENDING, RECLASSIFY_NEEDED
    }

    public enum DataOwnershipType {
        FIRST_PARTY, THIRD_PARTY, MIXED
    }
}
