package com.datasheild.discovery.repository;

import com.datasheild.discovery.entity.PIIFinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PIIFindingRepository extends JpaRepository<PIIFinding, UUID> {
    
    @Query("SELECT f FROM PIIFinding f WHERE f.scanId = :scanId ORDER BY f.severity DESC")
    List<PIIFinding> findByScanId(UUID scanId);
    
    @Query("SELECT f FROM PIIFinding f WHERE f.tenantId = :tenantId AND f.severity = :severity")
    List<PIIFinding> findByTenantIdAndSeverity(UUID tenantId, PIIFinding.Severity severity);
    
    @Query("SELECT f FROM PIIFinding f WHERE f.scanId = :scanId AND f.tableName = :tableName")
    List<PIIFinding> findByScanIdAndTableName(UUID scanId, String tableName);
    
    @Query("SELECT f FROM PIIFinding f WHERE f.tenantId = :tenantId AND f.piiType = :piiType")
    List<PIIFinding> findByTenantIdAndPiiType(UUID tenantId, PIIFinding.PIIType piiType);
    
    @Query("SELECT COUNT(f) FROM PIIFinding f WHERE f.scanId = :scanId AND f.severity = 'CRITICAL'")
    long countCriticalFindings(UUID scanId);
    
    @Query("SELECT f FROM PIIFinding f WHERE f.tenantId = :tenantId ORDER BY f.severity DESC, f.detectedAt DESC")
    List<PIIFinding> findAllByTenantId(UUID tenantId);
}
