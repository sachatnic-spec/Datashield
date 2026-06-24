package io.datasheild.consentservice.repository;

import io.datasheild.consentservice.entity.ConsentNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentNoticeRepository extends JpaRepository<ConsentNotice, UUID> {

    @Query("SELECT n FROM ConsentNotice n WHERE n.tenantId = :tenantId AND n.languageCode = :lang AND n.status = 'ACTIVE'")
    Optional<ConsentNotice> findActiveByTenantAndLanguage(@Param("tenantId") UUID tenantId, @Param("lang") String lang);

    @Query("SELECT n FROM ConsentNotice n WHERE n.tenantId = :tenantId AND n.status = 'ACTIVE'")
    List<ConsentNotice> findActiveByTenant(@Param("tenantId") UUID tenantId);

    @Query("SELECT n FROM ConsentNotice n WHERE n.tenantId = :tenantId AND n.versionNumber = :version AND n.status IN ('ACTIVE', 'DEPRECATED')")
    List<ConsentNotice> findByTenantAndVersion(@Param("tenantId") UUID tenantId, @Param("version") Integer version);
}
