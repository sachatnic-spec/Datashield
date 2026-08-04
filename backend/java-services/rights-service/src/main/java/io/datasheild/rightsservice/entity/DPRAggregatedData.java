package io.datasheild.rightsservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dpr_aggregated_data", schema = "rights",
       indexes = {
           @Index(name = "idx_agg_request", columnList = "dpr_request_id"),
           @Index(name = "idx_agg_processor", columnList = "processor_id"),
           @Index(name = "idx_agg_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DPRAggregatedData {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID dprRequestId;

    @Column(nullable = false)
    private UUID processorId;

    @Column(nullable = false, length = 100)
    private String processorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AggregationStatus status = AggregationStatus.PENDING;

    @Column
    private Long recordCount;

    @Column
    private Long dataSize;  // In bytes

    @Column(columnDefinition = "TEXT")
    private String dataUrl;  // Encrypted URL to aggregated data

    @Column(columnDefinition = "JSONB")
    private String dataCategories;  // JSON array: ["personal_info", "transaction_history", "preferences", ...]

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private String encryptionKeyId;

    @Column
    private String dataHash;  // SHA-256 hash for integrity verification

    public enum AggregationStatus {
        PENDING,        // Awaiting response from processor
        AGGREGATING,    // Currently aggregating data
        COMPLETED,      // Data aggregation complete
        FAILED,         // Aggregation failed
        ENCRYPTED,      // Data encrypted and ready
        DELIVERED       // Data delivered to DP
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = AggregationStatus.PENDING;
        }
    }
}
