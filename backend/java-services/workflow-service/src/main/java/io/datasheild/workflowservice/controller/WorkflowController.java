package io.datasheild.workflowservice.controller;

import io.datasheild.workflowservice.dto.InitiateWorkflowRequest;
import io.datasheild.workflowservice.entity.Workflow;
import io.datasheild.workflowservice.entity.WorkflowApproval;
import io.datasheild.workflowservice.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/workflows")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workflow Management", description = "Workflow orchestration, approval routing, state machine")
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    @Operation(summary = "Initiate a new workflow")
    public ResponseEntity<Workflow> initiateWorkflow(@RequestBody InitiateWorkflowRequest request) {
        log.info("POST /v1/workflows - Initiating workflow: {}", request.getWorkflowType());
        Workflow workflow = workflowService.initiateWorkflow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(workflow);
    }

    @PostMapping("/{workflowId}/start")
    @Operation(summary = "Start a workflow")
    public ResponseEntity<Workflow> startWorkflow(@PathVariable UUID workflowId, @RequestParam String executedBy) {
        log.info("POST /v1/workflows/{}/start - Starting workflow", workflowId);
        Workflow workflow = workflowService.startWorkflow(workflowId, executedBy);
        return ResponseEntity.ok(workflow);
    }

    @PostMapping("/{workflowId}/complete")
    @Operation(summary = "Complete a workflow")
    public ResponseEntity<Workflow> completeWorkflow(@PathVariable UUID workflowId, @RequestParam String result) {
        log.info("POST /v1/workflows/{}/complete - Completing workflow", workflowId);
        Workflow workflow = workflowService.completeWorkflow(workflowId, result);
        return ResponseEntity.ok(workflow);
    }

    @PostMapping("/{workflowId}/approve-request")
    @Operation(summary = "Request approval for a step")
    public ResponseEntity<WorkflowApproval> requestApproval(
        @PathVariable UUID workflowId,
        @RequestParam UUID stepId,
        @RequestParam String approverRole,
        @RequestParam(required = false) LocalDateTime dueDate) {
        log.info("POST /v1/workflows/{}/approve-request - Requesting approval", workflowId);
        WorkflowApproval approval = workflowService.requestApproval(workflowId, stepId, approverRole, dueDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(approval);
    }

    @PutMapping("/approvals/{approvalId}/approve")
    @Operation(summary = "Approve a step")
    public ResponseEntity<WorkflowApproval> approveStep(
        @PathVariable UUID approvalId,
        @RequestParam String approvedBy,
        @RequestParam String reason) {
        log.info("PUT /v1/workflows/approvals/{}/approve - Approving step", approvalId);
        WorkflowApproval approval = workflowService.approveStep(approvalId, approvedBy, reason);
        return ResponseEntity.ok(approval);
    }

    @PutMapping("/approvals/{approvalId}/reject")
    @Operation(summary = "Reject a step")
    public ResponseEntity<WorkflowApproval> rejectStep(
        @PathVariable UUID approvalId,
        @RequestParam String rejectedBy,
        @RequestParam String reason) {
        log.info("PUT /v1/workflows/approvals/{}/reject - Rejecting step", approvalId);
        WorkflowApproval approval = workflowService.rejectStep(approvalId, rejectedBy, reason);
        return ResponseEntity.ok(approval);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get workflows for an entity")
    public ResponseEntity<List<Workflow>> getWorkflowsByEntity(
        @PathVariable String entityType,
        @PathVariable UUID entityId) {
        log.info("GET /v1/workflows/entity/{}/{} - Retrieving workflows", entityType, entityId);
        List<Workflow> workflows = workflowService.getWorkflowsByEntity(entityType, entityId);
        return ResponseEntity.ok(workflows);
    }

    @GetMapping("/approvals/pending")
    @Operation(summary = "Get pending approvals for user")
    public ResponseEntity<List<WorkflowApproval>> getPendingApprovals(@RequestParam String approver) {
        log.info("GET /v1/workflows/approvals/pending - Retrieving pending approvals for: {}", approver);
        List<WorkflowApproval> approvals = workflowService.getPendingApprovalsForUser(approver);
        return ResponseEntity.ok(approvals);
    }
}
