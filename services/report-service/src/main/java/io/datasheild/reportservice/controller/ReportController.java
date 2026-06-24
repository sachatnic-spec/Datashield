package io.datasheild.reportservice.controller;

import io.datasheild.reportservice.entity.ComplianceReport;
import io.datasheild.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Compliance Reporting", description = "Generate and manage compliance reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Create scheduled compliance report")
    public ResponseEntity<ComplianceReport> createReport(
        @RequestParam UUID tenantId,
        @RequestParam String reportType,
        @RequestParam String reportTitle,
        @RequestParam ComplianceReport.ReportFrequency frequency,
        @RequestParam(required = false) String summary) {
        log.info("POST /v1/reports - Creating report: {}", reportType);
        ComplianceReport report = reportService.createReport(tenantId, reportType, reportTitle, frequency, summary);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @PostMapping("/{reportId}/complete")
    @Operation(summary = "Mark report as completed")
    public ResponseEntity<ComplianceReport> completeReport(
        @PathVariable UUID reportId,
        @RequestParam Double complianceScore,
        @RequestParam String fileLocation,
        @RequestParam(defaultValue = "PDF") String format) {
        log.info("POST /v1/reports/{}/complete - Marking report complete", reportId);
        ComplianceReport report = reportService.completeReport(reportId, complianceScore, fileLocation, format);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending reports for generation")
    public ResponseEntity<List<ComplianceReport>> getPendingReports() {
        log.info("GET /v1/reports/pending - Retrieving pending reports");
        List<ComplianceReport> reports = reportService.getPendingReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/score/{tenantId}")
    @Operation(summary = "Get tenant compliance score from reports")
    public ResponseEntity<Double> getComplianceScore(@PathVariable UUID tenantId) {
        log.info("GET /v1/reports/score/{} - Getting compliance score", tenantId);
        Double score = reportService.getTenantComplianceScore(tenantId);
        return ResponseEntity.ok(score);
    }
}
