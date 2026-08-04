package io.datasheild.tenantservice.repository;

import io.datasheild.tenantservice.entity.FeatureFlag;
import io.datasheild.tenantservice.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

    /**
     * Find feature flag by name and tier
     */
    Optional<FeatureFlag> findByFlagNameAndTier(String flagName, Tenant.TenantTier tier);

    /**
     * Find all active features for a tier
     */
    @Query("SELECT f FROM FeatureFlag f WHERE f.tier = :tier AND f.isActive = true ORDER BY f.flagName")
    List<FeatureFlag> findActiveFeaturesByTier(@Param("tier") Tenant.TenantTier tier);

    /**
     * Find all features for a tier (active and inactive)
     */
    @Query("SELECT f FROM FeatureFlag f WHERE f.tier = :tier ORDER BY f.flagName")
    List<FeatureFlag> findAllFeaturesByTier(@Param("tier") Tenant.TenantTier tier);

    /**
     * Find feature flags by name across all tiers
     */
    @Query("SELECT f FROM FeatureFlag f WHERE f.flagName = :flagName ORDER BY f.tier")
    List<FeatureFlag> findByFlagName(@Param("flagName") String flagName);

    /**
     * Count active features per tier
     */
    @Query("SELECT COUNT(f) FROM FeatureFlag f WHERE f.tier = :tier AND f.isActive = true")
    Long countActiveByTier(@Param("tier") Tenant.TenantTier tier);

    /**
     * Check if feature is enabled for tier
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FeatureFlag f " +
           "WHERE f.flagName = :flagName AND f.tier = :tier AND f.isActive = true")
    boolean isFeatureEnabledForTier(@Param("flagName") String flagName, @Param("tier") Tenant.TenantTier tier);
}
