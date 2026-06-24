package com.datasheild.riskscoring.repository;

import com.datasheild.riskscoring.entity.RiskScore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, UUID> {

    @Query("SELECT r FROM RiskScore r WHERE r.vendorId = :vendorId ORDER BY r.scoredAt ASC")
    List<RiskScore> findByVendorId(UUID vendorId);

    @Query("SELECT r FROM RiskScore r WHERE r.tenantId = :tenantId ORDER BY r.overallRiskScore DESC, r.scoredAt DESC")
    List<RiskScore> findByTenantId(UUID tenantId);

    @Query("SELECT r FROM RiskScore r WHERE r.tenantId = :tenantId ORDER BY r.overallRiskScore DESC, r.scoredAt DESC")
    List<RiskScore> findTopRisks(UUID tenantId, Pageable pageable);

    Optional<RiskScore> findTopByVendorIdOrderByScoredAtDesc(UUID vendorId);
}
