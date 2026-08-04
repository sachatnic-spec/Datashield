package io.datasheild.workflowservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_steps", schema = "workflow", indexes = {
    @Index(name = "idx_step_workflow_id", columnList = "workflow_id"),
    @Index(name = "idx_step_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "workflow_id", nullable = false, columnDefinition = "uuid")
    private UUID workflowId;

    @Column(name = "step_index", nullable = false)
    private Integer stepIndex;

    @Column(name = "step_name", nullable = false, length = 255)
    private String stepName;

    @Column(name = "step_type", nullable = false, length = 50)
    private String stepType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepStatus status;

    @Column(name = "assigned_to", length = 255)
    private String assignedTo;

    @Column(name = "action_data", columnDefinition = "jsonb")
    private String actionData;

    @Column(name = "result_data", columnDefinition = "jsonb")
    private String resultData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = StepStatus.PENDING;
        }
    }

    public enum StepStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        SKIPPED
    }
}
