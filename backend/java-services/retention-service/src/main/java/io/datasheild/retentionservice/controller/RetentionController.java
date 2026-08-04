package io.datasheild.retentionservice.controller;

import io.datasheild.retentionservice.entity.RetentionPolicy;
import io.datasheild.retentionservice.entity.DataErasureTask;
import io.datasheild.retentionservice.service.RetentionService;
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
@RequestMapping("/v1/retention")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Data Retention Management", description = "Retention policy enforcement and data lifecycle automation")
public class RetentionController {

    private final RetentionService retentionService;

    @PostMapping("/policies")
    @Operation(summary = "Create retention policy")
    public ResponseEntity<RetentionPolicy> createPolicy(
        @RequestParam String policyName,
        @RequestParam String sector,
        @RequestParam String dataCategory,
        @RequestParam Integer retentionDays,
        @RequestParam Integer maxRetentionDays,
        @RequestParam(required = false, defaultValue = "SECURE_SHRED") String disposalMethod) {
        log.info("POST /v1/retention/policies - Creating retention policy: {}", policyName);
        RetentionPolicy policy = retentionService.createPolicy(policyName, sector, dataCategory, 
                                                               retentionDays, maxRetentionDays, disposalMethod);
        return ResponseEntity.status(HttpStatus.CREATED).body(policy);
    }

    @PostMapping("/policies/{policyId}/approve")
    @Operation(summary = "Approve retention policy")
    public ResponseEntity<RetentionPolicy> approvePolicy(
        @PathVariable UUID policyId,
        @RequestParam String approvedBy) {
        log.info("POST /v1/retention/policies/{}/approve - Approving policy", policyId);
        RetentionPolicy policy = retentionService.approvePolicy(policyId, approvedBy);
        return ResponseEntity.ok(policy);
    }

    @PostMapping("/schedule-erasure")
    @Operation(summary = "Schedule data erasure task")
    public ResponseEntity<DataErasureTask> scheduleErasure(
        @RequestParam UUID tenantId,
        @RequestParam UUID policyId,
        @RequestParam String dataCategory,
        @RequestParam Integer recordCount,
        @RequestParam String scheduledForISO,
        @RequestParam(required = false, defaultValue = "SECURE_SHRED") String erasureMethod) {
        log.info("POST /v1/retention/schedule-erasure - Scheduling erasure");
        LocalDateTime scheduledFor = LocalDateTime.parse(scheduledForISO);
        DataErasureTask task = retentionService.scheduleErasure(tenantId, policyId, dataCategory, 
                                                                recordCount, scheduledFor, erasureMethod);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PostMapping("/execute/{taskId}")
    @Operation(summary = "Execute erasure task")
    public ResponseEntity<DataErasureTask> executeErasure(
        @PathVariable UUID taskId,
        @RequestParam String archiveLocation) {
        log.info("POST /v1/retention/execute/{} - Executing erasure task", taskId);
        DataErasureTask task = retentionService.executeErasure(taskId, archiveLocation);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/policies/active")
    @Operation(summary = "Get active retention policies")
    public ResponseEntity<List<RetentionPolicy>> getActivePolicies() {
        log.info("GET /v1/retention/policies/active - Retrieving active policies");
        List<RetentionPolicy> policies = retentionService.getActivePolicies();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/scheduled-tasks")
    @Operation(summary = "Get scheduled erasure tasks")
    public ResponseEntity<List<DataErasureTask>> getScheduledTasks() {
        log.info("GET /v1/retention/scheduled-tasks - Retrieving scheduled tasks");
        List<DataErasureTask> tasks = retentionService.getScheduledTasks();
        return ResponseEntity.ok(tasks);
    }
}
