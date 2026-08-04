package io.datasheild.breachservice.controller;

import io.datasheild.breachservice.dto.BreachIncidentResponse;
import io.datasheild.breachservice.dto.ReportBreachRequest;
import io.datasheild.breachservice.dto.SeverityScoreResponse;
import io.datasheild.breachservice.service.BreachService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/breach")
@RequiredArgsConstructor
@Slf4j
public class BreachController {

    private final BreachService breachService;

    @PostMapping("/incidents")
    public ResponseEntity<BreachIncidentResponse> reportBreach(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @RequestBody ReportBreachRequest request) {
        log.info("POST /v1/breach/incidents: tenant={}", tenantId);

        if (!request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        BreachIncidentResponse response = breachService.reportBreach(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/incidents/{incidentId}/contain")
    public ResponseEntity<BreachIncidentResponse> containBreach(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @PathVariable UUID incidentId,
            @RequestParam String containmentStrategy) {
        log.info("PUT /v1/breach/incidents/{}/contain: tenant={}", incidentId, tenantId);

        BreachIncidentResponse response = breachService.containBreach(tenantId, incidentId, containmentStrategy);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/incidents/{incidentId}/notify-dpbi")
    public ResponseEntity<BreachIncidentResponse> notifyDPBI(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @PathVariable UUID incidentId) {
        log.info("PUT /v1/breach/incidents/{}/notify-dpbi: tenant={}", incidentId, tenantId);

        BreachIncidentResponse response = breachService.notifyDPBI(tenantId, incidentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<BreachIncidentResponse>> getIncidents(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId) {
        log.info("GET /v1/breach/incidents: tenant={}", tenantId);

        List<BreachIncidentResponse> incidents = breachService.getIncidents(tenantId);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/{incidentId}")
    public ResponseEntity<BreachIncidentResponse> getIncident(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @PathVariable UUID incidentId) {
        log.info("GET /v1/breach/incidents/{}: tenant={}", incidentId, tenantId);

        BreachIncidentResponse response = breachService.getIncident(tenantId, incidentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/incidents/{incidentId}/severity")
    public ResponseEntity<SeverityScoreResponse> getSeverity(
            @RequestHeader(value = "X-Tenant-ID", required = true) UUID tenantId,
            @PathVariable UUID incidentId) {
        log.info("GET /v1/breach/incidents/{}/severity: tenant={}", incidentId, tenantId);

        SeverityScoreResponse response = breachService.getSeverityScore(tenantId, incidentId);
        return ResponseEntity.ok(response);
    }
}
