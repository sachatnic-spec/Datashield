package io.datasheild.grievanceservice.controller;

import io.datasheild.grievanceservice.entity.Grievance;
import io.datasheild.grievanceservice.entity.GrievanceActivity;
import io.datasheild.grievanceservice.service.GrievanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/grievances")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Grievance Management", description = "Grievance filing, tracking, and resolution (DPDP § 18)")
public class GrievanceController {

    private final GrievanceService grievanceService;

    @PostMapping
    @Operation(summary = "File a new grievance")
    public ResponseEntity<Grievance> fileGrievance(
        @RequestParam UUID tenantId,
        @RequestParam UUID dataPrincipalId,
        @RequestParam Grievance.GrievanceCategory category,
        @RequestParam Grievance.GrievanceChannel channel,
        @RequestParam String subject,
        @RequestParam String description) {
        log.info("POST /v1/grievances - Filing grievance for tenant: {}", tenantId);
        Grievance grievance = grievanceService.fileGrievance(tenantId, dataPrincipalId, category, channel, subject, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(grievance);
    }

    @PostMapping("/{grievanceId}/acknowledge")
    @Operation(summary = "Acknowledge grievance receipt")
    public ResponseEntity<Grievance> acknowledgeGrievance(
        @PathVariable UUID grievanceId,
        @RequestParam String acknowledgedBy) {
        log.info("POST /v1/grievances/{}/acknowledge - Acknowledging grievance", grievanceId);
        Grievance grievance = grievanceService.acknowledgeGrievance(grievanceId, acknowledgedBy);
        return ResponseEntity.ok(grievance);
    }

    @PostMapping("/{grievanceId}/resolve")
    @Operation(summary = "Resolve grievance")
    public ResponseEntity<Grievance> resolveGrievance(
        @PathVariable UUID grievanceId,
        @RequestParam String resolution,
        @RequestParam String resolvedBy) {
        log.info("POST /v1/grievances/{}/resolve - Resolving grievance", grievanceId);
        Grievance grievance = grievanceService.resolveGrievance(grievanceId, resolution, resolvedBy);
        return ResponseEntity.ok(grievance);
    }

    @PostMapping("/{grievanceId}/escalate")
    @Operation(summary = "Escalate grievance to DPO")
    public ResponseEntity<Grievance> escalateGrievance(
        @PathVariable UUID grievanceId,
        @RequestParam String escalationReason,
        @RequestParam String escalatedBy) {
        log.info("POST /v1/grievances/{}/escalate - Escalating grievance", grievanceId);
        Grievance grievance = grievanceService.escalateGrievance(grievanceId, escalationReason, escalatedBy);
        return ResponseEntity.ok(grievance);
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "List grievances by tenant")
    public ResponseEntity<Page<Grievance>> getGrievancesByTenant(
        @PathVariable UUID tenantId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        log.info("GET /v1/grievances/tenant/{} - Retrieving grievances", tenantId);
        Page<Grievance> grievances = grievanceService.getGrievancesByTenant(tenantId, PageRequest.of(page, size));
        return ResponseEntity.ok(grievances);
    }

    @GetMapping("/sla-breaches")
    @Operation(summary = "List SLA breaches (30-day deadline)")
    public ResponseEntity<List<Grievance>> getSLABreaches() {
        log.info("GET /v1/grievances/sla-breaches - Retrieving SLA breaches");
        List<Grievance> breaches = grievanceService.getSLABreaches();
        return ResponseEntity.ok(breaches);
    }

    @GetMapping("/{grievanceId}/history")
    @Operation(summary = "Get grievance activity history")
    public ResponseEntity<List<GrievanceActivity>> getGrievanceHistory(@PathVariable UUID grievanceId) {
        log.info("GET /v1/grievances/{}/history - Retrieving history", grievanceId);
        List<GrievanceActivity> history = grievanceService.getGrievanceHistory(grievanceId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/escalated/count")
    @Operation(summary = "Count escalated grievances")
    public ResponseEntity<Long> countEscalatedGrievances() {
        log.info("GET /v1/grievances/escalated/count - Counting escalated grievances");
        Long count = grievanceService.countEscalatedGrievances();
        return ResponseEntity.ok(count);
    }
}
