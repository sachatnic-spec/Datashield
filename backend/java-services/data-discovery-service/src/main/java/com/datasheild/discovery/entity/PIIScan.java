package com.datasheild.discovery.entity;

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
@Table(name = "pii_scan", schema = "discovery")
public class PIIScan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String scanName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanType scanType;

    @Column(nullable = false)
    private String targetDatabase;

    private String targetTable;

    @Column(columnDefinition = "TEXT")
    private String scanQuery;

    @Column(nullable = false)
    private Integer totalRecordsScanned;

    @Column(nullable = false)
    private Integer piiRecordsFound;

    @Column(nullable = false)
    private Integer criticalCount;

    @Column(nullable = false)
    private Integer highCount;

    @Column(nullable = false)
    private Integer mediumCount;

    @CreationTimestamp
    private LocalDateTime scanStartedAt;

    private LocalDateTime scanCompletedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ScanStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, FAILED
    }

    public enum ScanType {
        FULL_DATABASE, TABLE_SPECIFIC, CUSTOM_QUERY
    }
}
