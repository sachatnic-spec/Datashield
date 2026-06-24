package io.datasheild.tenantservice.repository;

import io.datasheild.tenantservice.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Find tenant by name
     */
    Optional<Tenant> findByName(String name);

    /**
     * Find tenant by schema name
     */
    Optional<Tenant> findBySchemaName(String schemaName);

    /**
     * Find all active tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionStatus = io.datasheild.tenantservice.entity.Tenant$SubscriptionStatus.ACTIVE")
    List<Tenant> findAllActive();

    /**
     * Find tenants by tier
     */
    Page<Tenant> findByTier(Tenant.TenantTier tier, Pageable pageable);

    /**
     * Find tenants by provisioning status
     */
    Page<Tenant> findByProvisioningStatus(Tenant.ProvisioningStatus status, Pageable pageable);

    /**
     * Find subscription status = ACTIVE and provisioning status = ACTIVE
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionStatus = io.datasheild.tenantservice.entity.Tenant$SubscriptionStatus.ACTIVE " +
           "AND t.provisioningStatus = io.datasheild.tenantservice.entity.Tenant$ProvisioningStatus.ACTIVE")
    List<Tenant> findFullyActivetenants();

    /**
     * Find tenants with contract end date approaching (< 30 days)
     */
    @Query("SELECT t FROM Tenant t WHERE t.contractEndDate IS NOT NULL " +
           "AND t.contractEndDate > :now AND t.contractEndDate <= :thirtyDaysFrom " +
           "AND t.subscriptionStatus = io.datasheild.tenantservice.entity.Tenant$SubscriptionStatus.ACTIVE")
    List<Tenant> findContractExpiringTenants(@Param("now") LocalDateTime now, @Param("thirtyDaysFrom") LocalDateTime thirtyDaysFrom);

    /**
     * Find expired contracts
     */
    @Query("SELECT t FROM Tenant t WHERE t.contractEndDate IS NOT NULL AND t.contractEndDate < :now " +
           "AND (t.autoRenewal = false OR t.subscriptionStatus != io.datasheild.tenantservice.entity.Tenant$SubscriptionStatus.ACTIVE)")
    List<Tenant> findExpiredContracts(@Param("now") LocalDateTime now);

    /**
     * Count tenants by tier
     */
    Long countByTier(Tenant.TenantTier tier);

    /**
     * Count active tenants
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.subscriptionStatus = io.datasheild.tenantservice.entity.Tenant$SubscriptionStatus.ACTIVE")
    Long countActivetenants();
}
