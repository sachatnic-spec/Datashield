package io.datasheild.workflowservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_approvals", schema = "workflow", indexes = {
    @Index(name = "idx_approval_workflow_id", columnList = "workflow_id"),
    @Index(name = "idx_approval_step_id", columnList = "step_id"),
    @Index(name = "idx_approval_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "workflow_id", nullable = false, columnDefinition = "uuid")
    private UUID workflowId;

    @Column(name = "step_id", nullable = false, columnDefinition = "uuid")
    private UUID stepId;

    @Column(name = "required_approver_role", nullable = false, length = 100)
    private String requiredApproverRole;

    @Column(name = "assigned_to", length = 255)
    private String assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatus status;

    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "approved_by", length = 255)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = ApprovalStatus.PENDING;
        }
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        WITHDRAWN
    }
}
