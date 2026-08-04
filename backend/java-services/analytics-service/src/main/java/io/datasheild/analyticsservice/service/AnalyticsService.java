package io.datasheild.analyticsservice.service;

import io.datasheild.analyticsservice.entity.ComplianceMetric;
import io.datasheild.analyticsservice.repository.ComplianceMetricRepository;
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
public class AnalyticsService {

    private final ComplianceMetricRepository metricRepository;

    @Transactional
    public ComplianceMetric recordMetric(UUID tenantId, String metricType, Double value, 
                                        String unit, String complianceSection) {
        log.info("Recording compliance metric: {} = {}", metricType, value);

        ComplianceMetric.MetricStatus status = determineStatus(metricType, value);

        ComplianceMetric metric = ComplianceMetric.builder()
            .tenantId(tenantId)
            .metricType(metricType)
            .metricValue(value)
            .unit(unit)
            .status(status)
            .complianceSection(complianceSection)
            .measuredAt(LocalDateTime.now())
            .build();

        metric = metricRepository.save(metric);
        log.info("Metric recorded: {} (status: {})", metric.getId(), status);

        return metric;
    }

    private ComplianceMetric.MetricStatus determineStatus(String metricType, Double value) {
        switch (metricType) {
            case "GRIEVANCE_SLA_COMPLIANCE":
                return value >= 95 ? ComplianceMetric.MetricStatus.OK :
                       value >= 85 ? ComplianceMetric.MetricStatus.WARNING :
                       ComplianceMetric.MetricStatus.CRITICAL;
            case "RETENTION_ACCURACY":
                return value >= 99 ? ComplianceMetric.MetricStatus.OK :
                       value >= 95 ? ComplianceMetric.MetricStatus.WARNING :
                       ComplianceMetric.MetricStatus.CRITICAL;
            case "VENDOR_RISK_CRITICAL_COUNT":
                return value == 0 ? ComplianceMetric.MetricStatus.OK :
                       value <= 5 ? ComplianceMetric.MetricStatus.WARNING :
                       ComplianceMetric.MetricStatus.CRITICAL;
            default:
                return ComplianceMetric.MetricStatus.OK;
        }
    }

    @Transactional(readOnly = true)
    public List<ComplianceMetric> getMetricsByTenant(UUID tenantId) {
        return metricRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<ComplianceMetric> getAlerts() {
        return metricRepository.findAlertMetrics();
    }

    @Transactional(readOnly = true)
    public Double getComplianceScore(UUID tenantId) {
        Double accessScore = metricRepository.getAverageMetric(tenantId, "DSAR_PROCESSING_TIME");
        Double retentionScore = metricRepository.getAverageMetric(tenantId, "RETENTION_ACCURACY");
        Double grievanceScore = metricRepository.getAverageMetric(tenantId, "GRIEVANCE_SLA_COMPLIANCE");

        return ((accessScore != null ? accessScore : 0) + 
                (retentionScore != null ? retentionScore : 0) + 
                (grievanceScore != null ? grievanceScore : 0)) / 3;
    }
}
