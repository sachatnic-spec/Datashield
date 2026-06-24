package com.datasheild.piidetection.controller;

import com.datasheild.piidetection.dto.BulkDetectionRequest;
import com.datasheild.piidetection.dto.DetectionRequest;
import com.datasheild.piidetection.dto.RedactionRequest;
import com.datasheild.piidetection.entity.PIIDetectionResult;
import com.datasheild.piidetection.service.PIIDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pii-detection")
@RequiredArgsConstructor
@Tag(name = "PII Detection", description = "PII detection and redaction endpoints")
public class PIIDetectionController {
    private final PIIDetectionService piiDetectionService;

    @PostMapping("/detect")
    @Operation(summary = "Detect the most likely PII category in a text payload")
    public ResponseEntity<PIIDetectionResult> detect(@Valid @RequestBody DetectionRequest request) {
        return ResponseEntity.ok(piiDetectionService.detectPII(request.getText(), request.getContext(), request.getTenantId()));
    }

    @PostMapping("/redact")
    @Operation(summary = "Redact sensitive values from text")
    public ResponseEntity<Map<String, String>> redact(@Valid @RequestBody RedactionRequest request) {
        return ResponseEntity.ok(Map.of(
            "originalText", request.getText(),
            "redactedText", piiDetectionService.redactPII(request.getText())
        ));
    }

    @PostMapping("/bulk-detect")
    @Operation(summary = "Run batch PII detection for multiple text samples")
    public ResponseEntity<List<PIIDetectionResult>> bulkDetect(@Valid @RequestBody BulkDetectionRequest request) {
        return ResponseEntity.ok(piiDetectionService.bulkDetect(request.getTexts(), request.getContext(), request.getTenantId()));
    }

    @GetMapping("/results/{tenantId}")
    @Operation(summary = "Fetch stored detection results for a tenant")
    public ResponseEntity<List<PIIDetectionResult>> getResults(@PathVariable UUID tenantId,
                                                               @RequestParam(required = false) Double minConfidence) {
        return ResponseEntity.ok(piiDetectionService.getResults(tenantId, minConfidence));
    }
}
