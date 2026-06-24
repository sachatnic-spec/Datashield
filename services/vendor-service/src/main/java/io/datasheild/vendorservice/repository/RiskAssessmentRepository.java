package io.datasheild.vendorservice.repository;

import io.datasheild.vendorservice.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {

    @Query("SELECT r FROM RiskAssessment r WHERE r.vendorId = :vendorId ORDER BY r.assessmentDate DESC")
    List<RiskAssessment> findByVendorId(@Param("vendorId") UUID vendorId);

    @Query("SELECT r FROM RiskAssessment r WHERE r.nextAssessmentDate IS NOT NULL AND r.nextAssessmentDate < :now ORDER BY r.nextAssessmentDate ASC")
    List<RiskAssessment> findDueForAssessment(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM RiskAssessment r WHERE r.riskLevel = io.datasheild.vendorservice.entity.Vendor$RiskLevel.CRITICAL ORDER BY r.overallScore DESC")
    List<RiskAssessment> findCriticalAssessments();

    @Query("SELECT r FROM RiskAssessment r WHERE r.assessmentDate IS NOT NULL AND r.assessmentDate > :since ORDER BY r.assessmentDate DESC")
    List<RiskAssessment> findRecentAssessments(@Param("since") LocalDateTime since);
}
