package io.datasheild.reportservice.repository;

import io.datasheild.reportservice.entity.ComplianceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, UUID> {

    @Query("SELECT cr FROM ComplianceReport cr WHERE cr.tenantId = :tenantId ORDER BY cr.generatedAt DESC")
    Page<ComplianceReport> findByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT cr FROM ComplianceReport cr WHERE cr.reportType = :type AND cr.status = io.datasheild.reportservice.entity.ComplianceReport$ReportStatus.COMPLETED ORDER BY cr.completedAt DESC")
    List<ComplianceReport> findCompletedReportsByType(@Param("type") String type);

    @Query("SELECT cr FROM ComplianceReport cr WHERE cr.status = io.datasheild.reportservice.entity.ComplianceReport$ReportStatus.SCHEDULED ORDER BY cr.generatedAt ASC")
    List<ComplianceReport> findPendingReports();

    @Query("SELECT AVG(cr.complianceScore) FROM ComplianceReport cr WHERE cr.tenantId = :tenantId AND cr.completedAt >= :since")
    Double getAverageComplianceScore(@Param("tenantId") UUID tenantId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(cr) FROM ComplianceReport cr WHERE cr.status = io.datasheild.reportservice.entity.ComplianceReport$ReportStatus.FAILED")
    Long countFailedReports();
}
