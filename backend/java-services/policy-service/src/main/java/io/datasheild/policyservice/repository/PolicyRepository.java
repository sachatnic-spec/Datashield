package io.datasheild.policyservice.repository;

import io.datasheild.policyservice.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, UUID> {

    @Query("SELECT p FROM Policy p WHERE p.status = io.datasheild.policyservice.entity.Policy$PolicyStatus.ACTIVE ORDER BY p.createdAt DESC")
    List<Policy> findActivePolicies();

    @Query("SELECT p FROM Policy p WHERE p.category = :category ORDER BY p.policyVersion DESC")
    Page<Policy> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT p FROM Policy p WHERE p.dpdpSection = :section")
    List<Policy> findByDPDPSection(@Param("section") String section);

    @Query("SELECT p FROM Policy p WHERE p.status = :status ORDER BY p.updatedAt DESC")
    Page<Policy> findByStatus(@Param("status") Policy.PolicyStatus status, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Policy p WHERE p.status = io.datasheild.policyservice.entity.Policy$PolicyStatus.ACTIVE")
    Long countActivePolicies();
}
