package io.datasheild.breachservice.repository;

import io.datasheild.breachservice.entity.BreachIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BreachIncidentRepository extends JpaRepository<BreachIncident, UUID> {

    @Query("SELECT b FROM BreachIncident b WHERE b.tenantId = :tenantId AND b.status NOT IN ('CLOSED') ORDER BY b.createdAt DESC")
    List<BreachIncident> findActiveByTenant(@Param("tenantId") UUID tenantId);

    @Query("SELECT b FROM BreachIncident b WHERE b.tenantId = :tenantId AND b.id = :incidentId")
    Optional<BreachIncident> findByTenantAndId(@Param("tenantId") UUID tenantId, @Param("incidentId") UUID incidentId);

    @Query("SELECT b FROM BreachIncident b WHERE b.tenantId = :tenantId AND b.dpbiDeadline <= :deadline AND b.status NOT IN ('NOTIFIED_DPBI', 'CLOSED') ORDER BY b.dpbiDeadline ASC")
    List<BreachIncident> findSLAViolatingIncidents(@Param("tenantId") UUID tenantId, @Param("deadline") LocalDateTime deadline);

    @Query("SELECT b FROM BreachIncident b WHERE b.tenantId = :tenantId AND b.severity = :severity AND b.status = 'REPORTED'")
    List<BreachIncident> findBySeverity(@Param("tenantId") UUID tenantId, @Param("severity") BreachIncident.SeverityLevel severity);

    @Query("SELECT COUNT(b) FROM BreachIncident b WHERE b.tenantId = :tenantId AND b.createdAt >= :since")
    long countSince(@Param("tenantId") UUID tenantId, @Param("since") LocalDateTime since);

    @Query("SELECT b FROM BreachIncident b WHERE b.tenantId = :tenantId AND b.status = :status ORDER BY b.createdAt DESC")
    List<BreachIncident> findByStatus(@Param("tenantId") UUID tenantId, @Param("status") BreachIncident.BreachStatus status);
}
