package io.datasheild.policyservice.repository;

import io.datasheild.policyservice.entity.PolicyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PolicyRuleRepository extends JpaRepository<PolicyRule, UUID> {

    @Query("SELECT r FROM PolicyRule r WHERE r.policyId = :policyId AND r.isActive = true ORDER BY r.priority ASC")
    List<PolicyRule> findActiveRulesByPolicy(@Param("policyId") UUID policyId);

    @Query("SELECT r FROM PolicyRule r WHERE r.conditionType = :conditionType ORDER BY r.priority ASC")
    List<PolicyRule> findByConditionType(@Param("conditionType") String conditionType);

    @Query("SELECT COUNT(r) FROM PolicyRule r WHERE r.policyId = :policyId")
    Long countByPolicy(@Param("policyId") UUID policyId);
}
