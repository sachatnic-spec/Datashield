package io.datasheild.workflowservice.repository;

import io.datasheild.workflowservice.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {

    @Query("SELECT s FROM WorkflowStep s WHERE s.workflowId = :workflowId ORDER BY s.stepIndex ASC")
    List<WorkflowStep> findByWorkflowId(@Param("workflowId") UUID workflowId);

    @Query("SELECT s FROM WorkflowStep s WHERE s.workflowId = :workflowId AND s.stepIndex = :stepIndex")
    Optional<WorkflowStep> findByWorkflowAndIndex(@Param("workflowId") UUID workflowId, @Param("stepIndex") Integer stepIndex);

    @Query("SELECT s FROM WorkflowStep s WHERE s.workflowId = :workflowId AND s.status != io.datasheild.workflowservice.entity.WorkflowStep$StepStatus.COMPLETED")
    List<WorkflowStep> findPendingSteps(@Param("workflowId") UUID workflowId);

    @Query("SELECT s FROM WorkflowStep s WHERE s.status = io.datasheild.workflowservice.entity.WorkflowStep$StepStatus.IN_PROGRESS")
    List<WorkflowStep> findInProgressSteps();

    @Query("SELECT COUNT(s) FROM WorkflowStep s WHERE s.workflowId = :workflowId AND s.status = io.datasheild.workflowservice.entity.WorkflowStep$StepStatus.COMPLETED")
    Long countCompletedSteps(@Param("workflowId") UUID workflowId);
}
