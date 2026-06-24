package io.datasheild.auth.repository;

import io.datasheild.auth.entity.MFADevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MFADeviceRepository extends JpaRepository<MFADevice, UUID> {

    @Query("SELECT m FROM MFADevice m WHERE m.userId = :userId AND m.tenantId = :tenantId AND m.active = true")
    List<MFADevice> findActiveDevicesByUserAndTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    @Query("SELECT m FROM MFADevice m WHERE m.userId = :userId AND m.tenantId = :tenantId AND m.type = :type AND m.active = true")
    Optional<MFADevice> findByUserTenantAndType(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId, @Param("type") MFADevice.MFAType type);

    @Query("SELECT m FROM MFADevice m WHERE m.userId = :userId AND m.tenantId = :tenantId AND m.verified = true AND m.active = true")
    List<MFADevice> findVerifiedDevicesByUserAndTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(m) FROM MFADevice m WHERE m.userId = :userId AND m.tenantId = :tenantId AND m.active = true")
    long countActiveDevicesByUserAndTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);
}
