package io.datasheild.tenantservice.repository;

import io.datasheild.tenantservice.entity.TenantProvisioningHistory;
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
public interface TenantProvisioningHistoryRepository extends JpaRepository<TenantProvisioningHistory, UUID> {

    /**
     * Find all provisioning history for a tenant
     */
    @Query("SELECT p FROM TenantProvisioningHistory p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC")
    List<TenantProvisioningHistory> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find paginated provisioning history for a tenant
     */
    @Query("SELECT p FROM TenantProvisioningHistory p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC")
    Page<TenantProvisioningHistory> findByTenantIdPageable(@Param("tenantId") UUID tenantId, Pageable pageable);

    /**
     * Find latest provisioning record for a tenant
     */
    @Query("SELECT p FROM TenantProvisioningHistory p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC LIMIT 1")
    Optional<TenantProvisioningHistory> findLatestByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Find failed provisioning attempts
     */
    @Query("SELECT p FROM TenantProvisioningHistory p WHERE p.tenantId = :tenantId " +
           "AND p.status = io.datasheild.tenantservice.entity.TenantProvisioningHistory$ProvisioningStatus.FAILED " +
           "ORDER BY p.createdAt DESC")
    List<TenantProvisioningHistory> findFailedProvisioningAttempts(@Param("tenantId") UUID tenantId);

    /**
     * Find provisioning records by status
     */
    @Query("SELECT p FROM TenantProvisioningHistory p WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<TenantProvisioningHistory> findByStatus(@Param("status") TenantProvisioningHistory.ProvisioningStatus status);

    /**
     * Find provisioning records in time range
     */
    @Query("SELECT p FROM TenantProvisioningHistory p WHERE p.tenantId = :tenantId " +
           "AND p.createdAt >= :from AND p.createdAt <= :to ORDER BY p.createdAt DESC")
    List<TenantProvisioningHistory> findInTimeRange(@Param("tenantId") UUID tenantId, 
                                                    @Param("from") LocalDateTime from, 
                                                    @Param("to") LocalDateTime to);

    /**
     * Count successful provisioning for tenant
     */
    @Query("SELECT COUNT(p) FROM TenantProvisioningHistory p WHERE p.tenantId = :tenantId " +
           "AND p.status = io.datasheild.tenantservice.entity.TenantProvisioningHistory$ProvisioningStatus.SUCCESS")
    Long countSuccessfulByTenantId(@Param("tenantId") UUID tenantId);
}
