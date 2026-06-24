package io.datasheild.rightsservice.repository;

import io.datasheild.rightsservice.entity.DPRActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DPRActivityRepository extends JpaRepository<DPRActivity, UUID> {

    @Query("SELECT a FROM DPRActivity a WHERE a.tenantId = :tenantId AND a.dprRequestId = :requestId ORDER BY a.createdAt DESC")
    List<DPRActivity> findByDPRRequest(@Param("tenantId") UUID tenantId, @Param("requestId") UUID requestId);

    @Query("SELECT a FROM DPRActivity a WHERE a.tenantId = :tenantId AND a.activityType = :type AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<DPRActivity> findByTypeSince(@Param("tenantId") UUID tenantId, @Param("type") DPRActivity.ActivityType type, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM DPRActivity a WHERE a.dprRequestId = :requestId AND a.activityType = :type")
    long countByRequestAndType(@Param("requestId") UUID requestId, @Param("type") DPRActivity.ActivityType type);
}
