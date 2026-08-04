package io.datasheild.analyticsservice.repository;

import io.datasheild.analyticsservice.entity.ComplianceMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ComplianceMetricRepository extends JpaRepository<ComplianceMetric, UUID> {

    @Query("SELECT cm FROM ComplianceMetric cm WHERE cm.tenantId = :tenantId ORDER BY cm.measuredAt DESC")
    List<ComplianceMetric> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT cm FROM ComplianceMetric cm WHERE cm.metricType = :type AND cm.measuredAt >= :since ORDER BY cm.measuredAt DESC")
    List<ComplianceMetric> findRecentMetrics(@Param("type") String type, @Param("since") LocalDateTime since);

    @Query("SELECT cm FROM ComplianceMetric cm WHERE cm.status = io.datasheild.analyticsservice.entity.ComplianceMetric$MetricStatus.CRITICAL OR cm.status = io.datasheild.analyticsservice.entity.ComplianceMetric$MetricStatus.BREACH")
    List<ComplianceMetric> findAlertMetrics();

    @Query("SELECT AVG(cm.metricValue) FROM ComplianceMetric cm WHERE cm.tenantId = :tenantId AND cm.metricType = :type")
    Double getAverageMetric(@Param("tenantId") UUID tenantId, @Param("type") String type);
}
