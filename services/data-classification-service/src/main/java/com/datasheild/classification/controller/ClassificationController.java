package com.datasheild.classification.controller;

import com.datasheild.classification.dto.ClassifyRequest;
import com.datasheild.classification.dto.DLPRuleRequest;
import com.datasheild.classification.entity.DataClassification;
import com.datasheild.classification.entity.DLPRule;
import com.datasheild.classification.service.ClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classification")
@RequiredArgsConstructor
@Tag(name = "Data Classification", description = "Data classification and DLP management endpoints")
public class ClassificationController {
    private final ClassificationService classificationService;

    @PostMapping("/classify")
    @Operation(summary = "Classify a data set by sensitivity level")
    public ResponseEntity<DataClassification> classifyDataSet(@RequestHeader("X-Tenant-ID") UUID tenantId,
                                                             @RequestBody ClassifyRequest request) {
        DataClassification classification = classificationService.classifyDataSet(
            tenantId,
            request.getDataSetName(),
            request.getTableName(),
            request.getRecordCount(),
            request.getPiiFieldCount(),
            request.getOwnershipType()
        );
        return ResponseEntity.ok(classification);
    }

    @PostMapping("/dlp-rules")
    @Operation(summary = "Create a new DLP rule")
    public ResponseEntity<DLPRule> createDLPRule(@RequestHeader("X-Tenant-ID") UUID tenantId,
                                                 @RequestBody DLPRuleRequest request) {
        DLPRule rule = classificationService.createDLPRule(
            tenantId,
            request.getRuleName(),
            request.getRuleType(),
            request.getAppliesToLevel(),
            request.getAction(),
            request.getPriority()
        );
        return ResponseEntity.ok(rule);
    }

    @PostMapping("/classify/{classificationId}/enforce-dlp")
    @Operation(summary = "Enforce DLP rules on a classification")
    public ResponseEntity<String> enforceDLP(@PathVariable UUID classificationId,
                                             @RequestHeader("X-Tenant-ID") UUID tenantId) {
        classificationService.enforceDLP(classificationId, tenantId);
        return ResponseEntity.ok("DLP enforcement applied");
    }

    @GetMapping("/tenant/{tenantId}/classifications")
    @Operation(summary = "Get all data classifications for a tenant")
    public ResponseEntity<List<DataClassification>> getTenantClassifications(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(classificationService.getTenantClassifications(tenantId));
    }

    @GetMapping("/dlp-rules/{tenantId}/{level}")
    @Operation(summary = "Get applicable DLP rules for sensitivity level")
    public ResponseEntity<List<DLPRule>> getApplicableRules(@PathVariable UUID tenantId,
                                                           @PathVariable DataClassification.SensitivityLevel level) {
        return ResponseEntity.ok(classificationService.getApplicableRules(tenantId, level));
    }

    @GetMapping("/summary/{tenantId}")
    @Operation(summary = "Get classification summary for tenant")
    public ResponseEntity<Map<String, Object>> getClassificationSummary(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(classificationService.getClassificationSummary(tenantId));
    }
}
