package io.datasheild.rightsservice.repository;

import io.datasheild.rightsservice.entity.DPRAggregatedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DPRAggregatedDataRepository extends JpaRepository<DPRAggregatedData, UUID> {

    @Query("SELECT d FROM DPRAggregatedData d WHERE d.dprRequestId = :requestId ORDER BY d.createdAt DESC")
    List<DPRAggregatedData> findByDPRRequest(@Param("requestId") UUID requestId);

    @Query("SELECT d FROM DPRAggregatedData d WHERE d.tenantId = :tenantId AND d.dprRequestId = :requestId AND d.processorId = :processorId")
    Optional<DPRAggregatedData> findByRequestAndProcessor(@Param("tenantId") UUID tenantId, @Param("requestId") UUID requestId, @Param("processorId") UUID processorId);

    @Query("SELECT d FROM DPRAggregatedData d WHERE d.dprRequestId = :requestId AND d.status NOT IN ('FAILED')")
    List<DPRAggregatedData> findSuccessfulByRequest(@Param("requestId") UUID requestId);

    @Query("SELECT COUNT(d) FROM DPRAggregatedData d WHERE d.dprRequestId = :requestId AND d.status = 'DELIVERED'")
    long countDeliveredByRequest(@Param("requestId") UUID requestId);
}
