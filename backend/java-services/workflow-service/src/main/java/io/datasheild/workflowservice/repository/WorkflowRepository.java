package io.datasheild.workflowservice.repository;

import io.datasheild.workflowservice.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    @Query("SELECT w FROM Workflow w WHERE w.entityType = :entityType AND w.entityId = :entityId ORDER BY w.createdAt DESC")
    List<Workflow> findByEntity(@Param("entityType") String entityType, @Param("entityId") UUID entityId);

    @Query("SELECT w FROM Workflow w WHERE w.workflowType = :workflowType ORDER BY w.createdAt DESC")
    Page<Workflow> findByType(@Param("workflowType") String workflowType, Pageable pageable);

    @Query("SELECT w FROM Workflow w WHERE w.status = :status ORDER BY w.createdAt DESC")
    List<Workflow> findByStatus(@Param("status") Workflow.WorkflowStatus status);

    @Query("SELECT w FROM Workflow w WHERE w.status IN (io.datasheild.workflowservice.entity.Workflow$WorkflowStatus.PENDING, " +
           "io.datasheild.workflowservice.entity.Workflow$WorkflowStatus.IN_PROGRESS, " +
           "io.datasheild.workflowservice.entity.Workflow$WorkflowStatus.AWAITING_APPROVAL) " +
           "AND w.updatedAt < :cutoffTime ORDER BY w.createdAt ASC")
    List<Workflow> findStaledWorkflows(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(w) FROM Workflow w WHERE w.status = :status")
    Long countByStatus(@Param("status") Workflow.WorkflowStatus status);
}
