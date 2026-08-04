package io.datasheild.workflowservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflows", schema = "workflow", indexes = {
    @Index(name = "idx_workflow_type", columnList = "workflow_type"),
    @Index(name = "idx_workflow_status", columnList = "status"),
    @Index(name = "idx_workflow_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "workflow_type", nullable = false, length = 100)
    private String workflowType;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false, columnDefinition = "uuid")
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkflowStatus status;

    @Column(name = "initiated_by", nullable = false, length = 255)
    private String initiatedBy;

    @Column(name = "current_step_index", nullable = false)
    private Integer currentStepIndex = 0;

    @Column(name = "total_steps", nullable = false)
    private Integer totalSteps;

    @Column(name = "context_data", columnDefinition = "jsonb")
    private String contextData;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    @Column(name = "timeout_minutes", nullable = false)
    private Integer timeoutMinutes = 60;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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
        if (status == null) {
            status = WorkflowStatus.PENDING;
        }
    }

    public enum WorkflowStatus {
        PENDING,
        STARTED,
        IN_PROGRESS,
        AWAITING_APPROVAL,
        APPROVED,
        REJECTED,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
