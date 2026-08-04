package com.datasheild.discovery.repository;

import com.datasheild.discovery.entity.PIIScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PIIScanRepository extends JpaRepository<PIIScan, UUID> {
    
    @Query("SELECT s FROM PIIScan s WHERE s.tenantId = :tenantId ORDER BY s.scanStartedAt DESC")
    List<PIIScan> findByTenantId(UUID tenantId);
    
    @Query("SELECT s FROM PIIScan s WHERE s.tenantId = :tenantId AND s.status = :status")
    List<PIIScan> findByTenantIdAndStatus(UUID tenantId, PIIScan.ScanStatus status);
    
    @Query("SELECT s FROM PIIScan s WHERE s.tenantId = :tenantId AND s.scanStartedAt >= :startDate ORDER BY s.scanStartedAt DESC")
    List<PIIScan> findRecentScans(UUID tenantId, LocalDateTime startDate);
    
    @Query("SELECT s FROM PIIScan s WHERE s.tenantId = :tenantId AND s.targetDatabase = :database")
    List<PIIScan> findByTenantIdAndDatabase(UUID tenantId, String database);
}
