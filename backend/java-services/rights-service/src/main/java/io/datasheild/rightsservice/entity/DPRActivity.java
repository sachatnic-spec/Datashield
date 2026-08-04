package io.datasheild.rightsservice.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dpr_activities", schema = "rights",
       indexes = {
           @Index(name = "idx_activity_dpr", columnList = "dpr_request_id"),
           @Index(name = "idx_activity_type", columnList = "activity_type"),
           @Index(name = "idx_activity_created", columnList = "created_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DPRActivity {

    @Id
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID dprRequestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(columnDefinition = "TEXT")
    private String activityDescription;

    @Column(length = 50)
    private String actor;  // SYSTEM, USER, DPO, PROCESSOR

    @Column(columnDefinition = "JSONB")
    private String activityMetadata;  // JSON: {systemId, processorId, reason, etc}

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private String auditTraceId;

    public enum ActivityType {
        REQUEST_RECEIVED,
        VERIFICATION_SENT,
        VERIFICATION_COMPLETED,
        VERIFICATION_FAILED,
        PROCESSING_STARTED,
        PROCESSOR_CONTACTED,
        DATA_AGGREGATED,
        RESPONSE_PREPARED,
        RESPONSE_SENT,
        REQUEST_COMPLETED,
        REQUEST_REJECTED,
        REQUEST_CANCELLED,
        EXTENSION_REQUESTED,
        EXTENSION_GRANTED,
        COMPLIANCE_CHECK_STARTED,
        COMPLIANCE_CHECK_COMPLETED
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
