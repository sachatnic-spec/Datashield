package io.datasheild.analyticsservice.controller;

import io.datasheild.analyticsservice.entity.ComplianceMetric;
import io.datasheild.analyticsservice.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Compliance Analytics", description = "Real-time compliance metrics and trending")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/metrics")
    @Operation(summary = "Record compliance metric")
    public ResponseEntity<ComplianceMetric> recordMetric(
        @RequestParam UUID tenantId,
        @RequestParam String metricType,
        @RequestParam Double value,
        @RequestParam(required = false, defaultValue = "%") String unit,
        @RequestParam String complianceSection) {
        log.info("POST /v1/analytics/metrics - Recording metric: {}", metricType);
        ComplianceMetric metric = analyticsService.recordMetric(tenantId, metricType, value, unit, complianceSection);
        return ResponseEntity.ok(metric);
    }

    @GetMapping("/metrics/{tenantId}")
    @Operation(summary = "Get metrics for tenant")
    public ResponseEntity<List<ComplianceMetric>> getMetrics(@PathVariable UUID tenantId) {
        log.info("GET /v1/analytics/metrics/{} - Retrieving metrics", tenantId);
        List<ComplianceMetric> metrics = analyticsService.getMetricsByTenant(tenantId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get critical/breach metrics")
    public ResponseEntity<List<ComplianceMetric>> getAlerts() {
        log.info("GET /v1/analytics/alerts - Retrieving alerts");
        List<ComplianceMetric> alerts = analyticsService.getAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/score/{tenantId}")
    @Operation(summary = "Get tenant compliance score (0-100)")
    public ResponseEntity<Double> getComplianceScore(@PathVariable UUID tenantId) {
        log.info("GET /v1/analytics/score/{} - Computing compliance score", tenantId);
        Double score = analyticsService.getComplianceScore(tenantId);
        return ResponseEntity.ok(score);
    }
}
