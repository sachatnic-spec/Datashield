package com.datasheild.lineage.repository;

import com.datasheild.lineage.entity.DataFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataFlowRepository extends JpaRepository<DataFlow, UUID> {
    
    @Query("SELECT f FROM DataFlow f WHERE f.tenantId = :tenantId")
    List<DataFlow> findByTenantId(UUID tenantId);
    
    @Query("SELECT f FROM DataFlow f WHERE f.sourceTable = :sourceTable AND f.tenantId = :tenantId")
    List<DataFlow> findDownstreamFlows(String sourceTable, UUID tenantId);
    
    @Query("SELECT f FROM DataFlow f WHERE f.targetTable = :targetTable AND f.tenantId = :tenantId")
    List<DataFlow> findUpstreamFlows(String targetTable, UUID tenantId);
    
    @Query("SELECT f FROM DataFlow f WHERE f.isThirdPartySharing = true AND f.tenantId = :tenantId")
    List<DataFlow> findThirdPartySharings(UUID tenantId);
    
    @Query("SELECT COUNT(f) FROM DataFlow f WHERE f.isThirdPartySharing = true AND f.tenantId = :tenantId")
    long countThirdPartySharings(UUID tenantId);
}
