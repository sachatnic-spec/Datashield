package com.datasheild.lineage.entity;

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
@Table(name = "data_flow", schema = "lineage", indexes = {
    @Index(name = "idx_source_target", columnList = "source_table,target_table"),
    @Index(name = "idx_tenant", columnList = "tenant_id")
})
public class DataFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String sourceTable;

    @Column(nullable = false)
    private String sourceDatabase;

    @Column(nullable = false)
    private String targetTable;

    @Column(nullable = false)
    private String targetDatabase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransformationType transformationType;

    @Column(columnDefinition = "TEXT")
    private String transformationLogic;

    @Column(nullable = false)
    private Boolean isThirdPartySharing;

    private String thirdPartyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataFlowStatus status;

    @Column(nullable = false)
    private Integer recordsProcessed;

    @CreationTimestamp
    private LocalDateTime recordedAt;

    public enum TransformationType {
        COPY, JOIN, AGGREGATE, FILTER, ENCRYPT, ANONYMIZE, CUSTOM
    }

    public enum DataFlowStatus {
        ACTIVE, INACTIVE, DEPRECATED, ARCHIVED
    }
}
