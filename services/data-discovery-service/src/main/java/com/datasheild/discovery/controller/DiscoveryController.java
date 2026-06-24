package com.datasheild.discovery.controller;

import com.datasheild.discovery.dto.ScanRequest;
import com.datasheild.discovery.entity.PIIFinding;
import com.datasheild.discovery.entity.PIIScan;
import com.datasheild.discovery.service.DiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/discovery")
@RequiredArgsConstructor
@Tag(name = "Data Discovery", description = "PII scanning and data discovery endpoints")
public class DiscoveryController {
    private final DiscoveryService discoveryService;

    @PostMapping("/scans/initiate")
    @Operation(summary = "Initiate a new PII scan")
    public ResponseEntity<PIIScan> initiateScan(@RequestHeader("X-Tenant-ID") UUID tenantId,
                                                 @RequestBody ScanRequest request) {
        PIIScan scan = discoveryService.initiateScan(
            tenantId,
            request.getScanName(),
            request.getScanType(),
            request.getTargetDatabase(),
            request.getTargetTable()
        );
        return ResponseEntity.ok(scan);
    }

    @PostMapping("/scans/{scanId}/execute")
    @Operation(summary = "Execute a scheduled PII scan")
    public ResponseEntity<String> executeScan(@PathVariable UUID scanId) {
        discoveryService.executeScan(scanId);
        return ResponseEntity.ok("Scan execution started");
    }

    @GetMapping("/scans/tenant/{tenantId}")
    @Operation(summary = "Get all scans for a tenant")
    public ResponseEntity<List<PIIScan>> getTenantScans(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(discoveryService.getTenantScans(tenantId));
    }

    @GetMapping("/scans/{scanId}/findings")
    @Operation(summary = "Get findings from a completed scan")
    public ResponseEntity<List<PIIFinding>> getScanFindings(@PathVariable UUID scanId) {
        return ResponseEntity.ok(discoveryService.getScanFindings(scanId));
    }

    @GetMapping("/hotspots/{tenantId}")
    @Operation(summary = "Generate PII hotspot report")
    public ResponseEntity<Map<String, Object>> generateHotspotReport(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(discoveryService.generateHotspotReport(tenantId));
    }
}
