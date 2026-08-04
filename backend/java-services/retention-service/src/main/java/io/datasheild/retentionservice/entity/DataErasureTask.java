package io.datasheild.retentionservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "data_erasure_task", schema = "retention")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataErasureTask {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID policyId;

    @Column(nullable = false, length = 100)
    private String dataCategory;

    @Column(nullable = false)
    private Integer recordsToErase;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.SCHEDULED;

    @Column(nullable = false)
    private LocalDateTime scheduledFor;

    private LocalDateTime executedAt;

    @Column(length = 1000)
    private String erasureMethod;

    @Column(length = 500)
    private String archiveLocation;

    private Integer recordsErased;

    private Integer failedCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TaskStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, FAILED, ARCHIVED
    }
}
