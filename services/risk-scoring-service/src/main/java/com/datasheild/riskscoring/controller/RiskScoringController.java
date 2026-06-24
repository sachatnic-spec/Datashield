package com.datasheild.riskscoring.controller;

import com.datasheild.riskscoring.dto.RiskScoreRequest;
import com.datasheild.riskscoring.dto.VendorRiskResponse;
import com.datasheild.riskscoring.service.RiskScoringService;
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
@RequestMapping("/api/v1/risk-scoring")
@RequiredArgsConstructor
@Tag(name = "Risk Scoring", description = "Vendor risk scoring and trend endpoints")
public class RiskScoringController {
    private final RiskScoringService riskScoringService;

    @PostMapping("/score-vendor")
    @Operation(summary = "Score a vendor using weighted risk factors")
    public ResponseEntity<VendorRiskResponse> scoreVendor(@Valid @RequestBody RiskScoreRequest request) {
        return ResponseEntity.ok(riskScoringService.scoreVendor(request.getVendorId(), request));
    }

    @GetMapping("/trend/{vendorId}")
    @Operation(summary = "Predict vendor risk trend from historical scores")
    public ResponseEntity<Map<String, Object>> getTrend(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(riskScoringService.predictRiskTrend(vendorId, null));
    }

    @GetMapping("/top-risks/{tenantId}")
    @Operation(summary = "Fetch highest-risk vendors for a tenant")
    public ResponseEntity<List<VendorRiskResponse>> getTopRisks(@PathVariable UUID tenantId,
                                                                @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(riskScoringService.getTopRisks(tenantId, limit));
    }
}
