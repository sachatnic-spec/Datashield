package io.datasheild.reportservice.service;

import io.datasheild.reportservice.entity.ComplianceReport;
import io.datasheild.reportservice.repository.ComplianceReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ComplianceReportRepository reportRepository;

    @Transactional
    public ComplianceReport createReport(UUID tenantId, String reportType, String reportTitle,
                                        ComplianceReport.ReportFrequency frequency, String summary) {
        log.info("Creating compliance report: {} for tenant: {}", reportType, tenantId);

        ComplianceReport report = ComplianceReport.builder()
            .tenantId(tenantId)
            .reportType(reportType)
            .reportTitle(reportTitle)
            .status(ComplianceReport.ReportStatus.SCHEDULED)
            .generatedAt(LocalDateTime.now())
            .frequency(frequency)
            .summary(summary)
            .build();

        report = reportRepository.save(report);
        log.info("Report scheduled: {}", report.getId());
        return report;
    }

    @Transactional
    public ComplianceReport completeReport(UUID reportId, Double complianceScore, 
                                          String fileLocation, String format) {
        log.info("Completing report: {}", reportId);

        ComplianceReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        report.setStatus(ComplianceReport.ReportStatus.COMPLETED);
        report.setCompletedAt(LocalDateTime.now());
        report.setComplianceScore(complianceScore);
        report.setFileLocation(fileLocation);
        report.setFileFormat(format);

        report = reportRepository.save(report);
        log.info("Report completed: {}, score: {}, location: {}", reportId, complianceScore, fileLocation);

        return report;
    }

    @Transactional
    public ComplianceReport failReport(UUID reportId, String reason) {
        log.error("Marking report as failed: {}, reason: {}", reportId, reason);

        ComplianceReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        report.setStatus(ComplianceReport.ReportStatus.FAILED);
        report.setSummary("Failed: " + reason);

        report = reportRepository.save(report);
        return report;
    }

    @Transactional(readOnly = true)
    public List<ComplianceReport> getPendingReports() {
        return reportRepository.findPendingReports();
    }

    @Transactional(readOnly = true)
    public Double getTenantComplianceScore(UUID tenantId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return reportRepository.getAverageComplianceScore(tenantId, thirtyDaysAgo);
    }

    @Transactional(readOnly = true)
    public Long getFailedReportCount() {
        return reportRepository.countFailedReports();
    }
}
