package com.datasheild.classification.repository;

import com.datasheild.classification.entity.DLPRule;
import com.datasheild.classification.entity.DataClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DLPRuleRepository extends JpaRepository<DLPRule, UUID> {
    
    @Query("SELECT r FROM DLPRule r WHERE r.tenantId = :tenantId AND r.enabled = true")
    List<DLPRule> findActiveDLPRules(UUID tenantId);
    
    @Query("SELECT r FROM DLPRule r WHERE r.tenantId = :tenantId AND r.appliesTo = :level ORDER BY r.priority ASC")
    List<DLPRule> findRulesByTenantAndLevel(UUID tenantId, DataClassification.SensitivityLevel level);
    
    @Query("SELECT r FROM DLPRule r WHERE r.tenantId = :tenantId AND r.status = 'ACTIVE' ORDER BY r.priority ASC")
    List<DLPRule> findActiveDLPRulesByTenant(UUID tenantId);
}

