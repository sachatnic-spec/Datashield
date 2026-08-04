package io.datasheild.rightsservice.repository;

import io.datasheild.rightsservice.entity.DPRRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DPRRequestRepository extends JpaRepository<DPRRequest, UUID> {

    @Query("SELECT r FROM DPRRequest r WHERE r.tenantId = :tenantId AND r.dataPrincipalId = :dpId AND r.status NOT IN ('CANCELLED', 'REJECTED') ORDER BY r.createdAt DESC")
    List<DPRRequest> findActiveByDataPrincipal(@Param("tenantId") UUID tenantId, @Param("dpId") UUID dpId);

    @Query("SELECT r FROM DPRRequest r WHERE r.tenantId = :tenantId AND r.id = :requestId")
    Optional<DPRRequest> findByTenantAndId(@Param("tenantId") UUID tenantId, @Param("requestId") UUID requestId);

    @Query("SELECT r FROM DPRRequest r WHERE r.tenantId = :tenantId AND r.status = 'VERIFICATION_PENDING' AND r.slaDeadline > :now ORDER BY r.createdAt ASC")
    List<DPRRequest> findPendingVerification(@Param("tenantId") UUID tenantId, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM DPRRequest r WHERE r.tenantId = :tenantId AND r.status = 'PROCESSING' AND r.slaDeadline <= :deadline ORDER BY r.slaDeadline ASC")
    List<DPRRequest> findSLAViolatingRequests(@Param("tenantId") UUID tenantId, @Param("deadline") LocalDateTime deadline);

    @Query("SELECT r FROM DPRRequest r WHERE r.tenantId = :tenantId AND r.requestType = :type AND r.status IN ('PROCESSING', 'AWAITING_RESPONSE')")
    List<DPRRequest> findActiveByType(@Param("tenantId") UUID tenantId, @Param("type") DPRRequest.DPRType type);

    @Query("SELECT COUNT(r) FROM DPRRequest r WHERE r.tenantId = :tenantId AND r.dataPrincipalId = :dpId AND r.requestType = :type AND r.status NOT IN ('CANCELLED', 'REJECTED') AND r.createdAt > :since")
    long countDuplicateRequestsSince(@Param("tenantId") UUID tenantId, @Param("dpId") UUID dpId, @Param("type") DPRRequest.DPRType type, @Param("since") LocalDateTime since);

    @Query("SELECT r FROM DPRRequest r WHERE r.tenantId = :tenantId AND r.status = 'COMPLETED' AND r.completedAt >= :since ORDER BY r.completedAt DESC")
    List<DPRRequest> findCompletedSince(@Param("tenantId") UUID tenantId, @Param("since") LocalDateTime since);
}
