package io.datasheild.workflowservice.repository;

import io.datasheild.workflowservice.entity.WorkflowApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowApprovalRepository extends JpaRepository<WorkflowApproval, UUID> {

    @Query("SELECT a FROM WorkflowApproval a WHERE a.workflowId = :workflowId ORDER BY a.createdAt DESC")
    List<WorkflowApproval> findByWorkflowId(@Param("workflowId") UUID workflowId);

    @Query("SELECT a FROM WorkflowApproval a WHERE a.stepId = :stepId")
    List<WorkflowApproval> findByStepId(@Param("stepId") UUID stepId);

    @Query("SELECT a FROM WorkflowApproval a WHERE a.status = io.datasheild.workflowservice.entity.WorkflowApproval$ApprovalStatus.PENDING")
    List<WorkflowApproval> findPendingApprovals();

    @Query("SELECT a FROM WorkflowApproval a WHERE a.assignedTo = :approver AND a.status = io.datasheild.workflowservice.entity.WorkflowApproval$ApprovalStatus.PENDING")
    List<WorkflowApproval> findPendingApprovalsFor(@Param("approver") String approver);

    @Query("SELECT a FROM WorkflowApproval a WHERE a.dueDate IS NOT NULL AND a.dueDate < :now AND " +
           "a.status = io.datasheild.workflowservice.entity.WorkflowApproval$ApprovalStatus.PENDING")
    List<WorkflowApproval> findOverdueApprovals(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(a) FROM WorkflowApproval a WHERE a.workflowId = :workflowId AND a.status = io.datasheild.workflowservice.entity.WorkflowApproval$ApprovalStatus.PENDING")
    Long countPendingForWorkflow(@Param("workflowId") UUID workflowId);
}
