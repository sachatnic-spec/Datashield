package io.datasheild.workflowservice.service;

import io.datasheild.workflowservice.dto.InitiateWorkflowRequest;
import io.datasheild.workflowservice.entity.Workflow;
import io.datasheild.workflowservice.entity.WorkflowApproval;
import io.datasheild.workflowservice.entity.WorkflowStep;
import io.datasheild.workflowservice.repository.WorkflowApprovalRepository;
import io.datasheild.workflowservice.repository.WorkflowRepository;
import io.datasheild.workflowservice.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowApprovalRepository approvalRepository;

    @Transactional
    public Workflow initiateWorkflow(InitiateWorkflowRequest request) {
        log.info("Initiating workflow: {}", request.getWorkflowType());

        Workflow workflow = Workflow.builder()
            .workflowType(request.getWorkflowType())
            .entityType(request.getEntityType())
            .entityId(request.getEntityId())
            .initiatedBy(request.getInitiatedBy())
            .currentStepIndex(0)
            .totalSteps(request.getSteps().size())
            .contextData(request.getContextData())
            .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3)
            .timeoutMinutes(request.getTimeoutMinutes() != null ? request.getTimeoutMinutes() : 60)
            .build();

        workflow = workflowRepository.save(workflow);

        // Create workflow steps
        for (int i = 0; i < request.getSteps().size(); i++) {
            InitiateWorkflowRequest.StepDefinition stepDef = request.getSteps().get(i);
            WorkflowStep step = WorkflowStep.builder()
                .workflowId(workflow.getId())
                .stepIndex(i)
                .stepName(stepDef.getStepName())
                .stepType(stepDef.getStepType())
                .actionData(stepDef.getActionData())
                .build();
            stepRepository.save(step);
        }

        log.info("Workflow initiated: {} with {} steps", workflow.getId(), request.getSteps().size());
        return workflow;
    }

    @Transactional
    public Workflow startWorkflow(UUID workflowId, String executedBy) {
        log.info("Starting workflow: {}", workflowId);

        Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

        workflow.setStatus(Workflow.WorkflowStatus.STARTED);
        workflow.setStartedAt(LocalDateTime.now());
        workflow = workflowRepository.save(workflow);

        log.info("Workflow started: {}", workflowId);
        return workflow;
    }

    @Transactional
    public Workflow completeWorkflow(UUID workflowId, String result) {
        log.info("Completing workflow: {}", workflowId);

        Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

        workflow.setStatus(Workflow.WorkflowStatus.COMPLETED);
        workflow.setCompletedAt(LocalDateTime.now());
        workflow = workflowRepository.save(workflow);

        log.info("Workflow completed: {}", workflowId);
        return workflow;
    }

    @Transactional
    public WorkflowApproval requestApproval(UUID workflowId, UUID stepId, String approverRole, LocalDateTime dueDate) {
        log.info("Requesting approval for step: {} in workflow: {}", stepId, workflowId);

        WorkflowApproval approval = WorkflowApproval.builder()
            .workflowId(workflowId)
            .stepId(stepId)
            .requiredApproverRole(approverRole)
            .dueDate(dueDate)
            .build();

        approval = approvalRepository.save(approval);
        log.info("Approval requested: {}", approval.getId());

        return approval;
    }

    @Transactional
    public WorkflowApproval approveStep(UUID approvalId, String approvedBy, String reason) {
        log.info("Approving step: {}", approvalId);

        WorkflowApproval approval = approvalRepository.findById(approvalId)
            .orElseThrow(() -> new RuntimeException("Approval not found: " + approvalId));

        approval.setStatus(WorkflowApproval.ApprovalStatus.APPROVED);
        approval.setApprovedBy(approvedBy);
        approval.setApprovalReason(reason);
        approval.setApprovedAt(LocalDateTime.now());

        approval = approvalRepository.save(approval);
        log.info("Approval granted: {}", approvalId);

        return approval;
    }

    @Transactional
    public WorkflowApproval rejectStep(UUID approvalId, String rejectedBy, String reason) {
        log.info("Rejecting step: {}", approvalId);

        WorkflowApproval approval = approvalRepository.findById(approvalId)
            .orElseThrow(() -> new RuntimeException("Approval not found: " + approvalId));

        approval.setStatus(WorkflowApproval.ApprovalStatus.REJECTED);
        approval.setApprovedBy(rejectedBy);
        approval.setRejectionReason(reason);
        approval.setApprovedAt(LocalDateTime.now());

        approval = approvalRepository.save(approval);
        log.info("Approval rejected: {}", approvalId);

        return approval;
    }

    @Transactional(readOnly = true)
    public List<Workflow> getWorkflowsByEntity(String entityType, UUID entityId) {
        return workflowRepository.findByEntity(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<WorkflowApproval> getPendingApprovalsForUser(String approver) {
        return approvalRepository.findPendingApprovalsFor(approver);
    }

    @Transactional(readOnly = true)
    public List<Workflow> getStaledWorkflows(int minutesOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutesOld);
        return workflowRepository.findStaledWorkflows(cutoff);
    }
}
