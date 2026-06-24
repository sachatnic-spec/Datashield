package com.datasheild.piidetection.repository;

import com.datasheild.piidetection.entity.PIIDetectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PIIDetectionResultRepository extends JpaRepository<PIIDetectionResult, UUID> {

    @Query("SELECT p FROM PIIDetectionResult p WHERE p.tenantId = :tenantId ORDER BY p.detectedAt DESC")
    List<PIIDetectionResult> findByTenantId(UUID tenantId);

    @Query("SELECT p FROM PIIDetectionResult p WHERE p.confidenceScore >= :confidenceScore ORDER BY p.confidenceScore DESC, p.detectedAt DESC")
    List<PIIDetectionResult> findByConfidenceScore(Double confidenceScore);
}
