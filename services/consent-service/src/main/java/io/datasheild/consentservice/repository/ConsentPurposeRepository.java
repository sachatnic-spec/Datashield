package io.datasheild.consentservice.repository;

import io.datasheild.consentservice.entity.ConsentPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentPurposeRepository extends JpaRepository<ConsentPurpose, UUID> {

    @Query("SELECT p FROM ConsentPurpose p WHERE p.tenantId = :tenantId AND p.purposeCode = :code AND p.status = 'ACTIVE'")
    Optional<ConsentPurpose> findByTenantAndCode(@Param("tenantId") UUID tenantId, @Param("code") String code);

    @Query("SELECT p FROM ConsentPurpose p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    List<ConsentPurpose> findActiveByTenant(@Param("tenantId") UUID tenantId);

    @Query("SELECT p FROM ConsentPurpose p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC")
    List<ConsentPurpose> findAllByTenant(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(p) FROM ConsentPurpose p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE'")
    long countActivePurposesByTenant(@Param("tenantId") UUID tenantId);
}
