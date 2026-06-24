package io.datasheild.consentservice.repository;

import io.datasheild.consentservice.entity.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {

    @Query("SELECT c FROM ConsentRecord c WHERE c.tenantId = :tenantId AND c.dataPrincipalId = :dpId AND c.status = 'GRANTED' AND (c.expiresAt IS NULL OR c.expiresAt > :now)")
    List<ConsentRecord> findActiveConsentsByDataPrincipal(@Param("tenantId") UUID tenantId, @Param("dpId") UUID dpId, @Param("now") LocalDateTime now);

    @Query("SELECT c FROM ConsentRecord c WHERE c.tenantId = :tenantId AND c.dataPrincipalId = :dpId AND c.purposeId = :purposeId AND c.status = 'GRANTED'")
    Optional<ConsentRecord> findByTenantAndDPAndPurpose(@Param("tenantId") UUID tenantId, @Param("dpId") UUID dpId, @Param("purposeId") UUID purposeId);

    @Query("SELECT c FROM ConsentRecord c WHERE c.tenantId = :tenantId AND c.dataPrincipalId = :dpId AND c.purposeId = :purposeId AND c.status IN ('GRANTED', 'WITHDRAWN')")
    List<ConsentRecord> findHistoryByDataPrincipalAndPurpose(@Param("tenantId") UUID tenantId, @Param("dpId") UUID dpId, @Param("purposeId") UUID purposeId);

    @Query("SELECT COUNT(c) FROM ConsentRecord c WHERE c.tenantId = :tenantId AND c.purposeId = :purposeId AND c.status = 'GRANTED'")
    long countActiveConsentsByPurpose(@Param("tenantId") UUID tenantId, @Param("purposeId") UUID purposeId);

    @Query("SELECT c FROM ConsentRecord c WHERE c.tenantId = :tenantId AND c.expiresAt < :now AND c.status = 'GRANTED'")
    List<ConsentRecord> findExpiredConsents(@Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    @Query("SELECT c FROM ConsentRecord c WHERE c.tenantId = :tenantId AND c.purposeId = :purposeId AND c.status = 'GRANTED'")
    List<ConsentRecord> findAllActiveConsentsByPurpose(@Param("tenantId") UUID tenantId, @Param("purposeId") UUID purposeId);
}
