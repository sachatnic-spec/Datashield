package com.datasheild.lineage.controller;

import com.datasheild.lineage.dto.DataFlowRequest;
import com.datasheild.lineage.entity.DataFlow;
import com.datasheild.lineage.service.LineageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lineage")
@RequiredArgsConstructor
@Tag(name = "Data Lineage", description = "Data flow tracking and provenance endpoints")
public class LineageController {
    private final LineageService lineageService;

    @PostMapping("/flows")
    @Operation(summary = "Record a new data flow")
    public ResponseEntity<DataFlow> recordDataFlow(@RequestHeader("X-Tenant-ID") UUID tenantId,
                                                   @RequestBody DataFlowRequest request) {
        DataFlow flow = lineageService.recordDataFlow(
            tenantId,
            request.getSourceTable(),
            request.getSourceDatabase(),
            request.getTargetTable(),
            request.getTargetDatabase(),
            request.getTransformationType(),
            request.getIsThirdPartySharing(),
            request.getThirdPartyName()
        );
        return ResponseEntity.ok(flow);
    }

    @GetMapping("/flows/tenant/{tenantId}")
    @Operation(summary = "Get all data flows for a tenant")
    public ResponseEntity<List<DataFlow>> getAllDataFlows(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(lineageService.getAllDataFlows(tenantId));
    }

    @GetMapping("/flows/downstream/{tenantId}/{sourceTable}")
    @Operation(summary = "Get downstream data lineage")
    public ResponseEntity<List<DataFlow>> getDownstreamLineage(@PathVariable UUID tenantId,
                                                               @PathVariable String sourceTable) {
        return ResponseEntity.ok(lineageService.getDownstreamLineage(tenantId, sourceTable));
    }

    @GetMapping("/flows/upstream/{tenantId}/{targetTable}")
    @Operation(summary = "Get upstream data lineage")
    public ResponseEntity<List<DataFlow>> getUpstreamLineage(@PathVariable UUID tenantId,
                                                             @PathVariable String targetTable) {
        return ResponseEntity.ok(lineageService.getUpstreamLineage(tenantId, targetTable));
    }

    @GetMapping("/graph/{tenantId}")
    @Operation(summary = "Get complete data lineage graph")
    public ResponseEntity<Map<String, Object>> getLineageGraph(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(lineageService.getDataLineageGraph(tenantId));
    }

    @GetMapping("/compliance-impact/{tenantId}")
    @Operation(summary = "Analyze compliance impact and third-party sharing risks")
    public ResponseEntity<Map<String, Object>> getComplianceImpact(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(lineageService.getComplianceImpactAnalysis(tenantId));
    }

    @GetMapping("/affected-principals/{tenantId}/{table}")
    @Operation(summary = "Get data principals affected by data flows")
    public ResponseEntity<List<String>> getAffectedPrincipals(@PathVariable UUID tenantId,
                                                             @PathVariable String table) {
        return ResponseEntity.ok(lineageService.getDataPrincipalsAffected(tenantId, table));
    }
}
