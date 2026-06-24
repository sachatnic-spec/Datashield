package com.datasheild.classification.repository;

import com.datasheild.classification.entity.DataClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataClassificationRepository extends JpaRepository<DataClassification, UUID> {
    
    @Query("SELECT c FROM DataClassification c WHERE c.tenantId = :tenantId")
    List<DataClassification> findByTenantId(UUID tenantId);
    
    @Query("SELECT c FROM DataClassification c WHERE c.tenantId = :tenantId AND c.sensitivityLevel = :level")
    List<DataClassification> findByTenantIdAndSensitivity(UUID tenantId, DataClassification.SensitivityLevel level);
    
    @Query("SELECT c FROM DataClassification c WHERE c.tenantId = :tenantId AND c.status = :status")
    List<DataClassification> findByTenantIdAndStatus(UUID tenantId, DataClassification.ClassificationStatus status);
    
    @Query("SELECT COUNT(c) FROM DataClassification c WHERE c.tenantId = :tenantId AND c.sensitivityLevel = 'RESTRICTED'")
    long countRestrictedData(UUID tenantId);
}
