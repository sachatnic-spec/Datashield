package com.datasheild.aianalysis.repository;

import com.datasheild.aianalysis.entity.AnomalyDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnomalyDetectionRepository extends JpaRepository<AnomalyDetection, UUID> {
    
    @Query("SELECT a FROM AnomalyDetection a WHERE a.tenantId = :tenantId ORDER BY a.detectedAt DESC")
    List<AnomalyDetection> findByTenantId(UUID tenantId);
    
    @Query("SELECT a FROM AnomalyDetection a WHERE a.tenantId = :tenantId AND a.severity = :severity ORDER BY a.detectedAt DESC")
    List<AnomalyDetection> findByTenantIdAndSeverity(UUID tenantId, AnomalyDetection.AnomalySeverity severity);
    
    @Query("SELECT a FROM AnomalyDetection a WHERE a.tenantId = :tenantId AND a.detectedAt >= :since")
    List<AnomalyDetection> findRecentAnomalies(UUID tenantId, LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM AnomalyDetection a WHERE a.tenantId = :tenantId AND a.severity = 'CRITICAL'")
    long countCriticalAnomalies(UUID tenantId);
}
