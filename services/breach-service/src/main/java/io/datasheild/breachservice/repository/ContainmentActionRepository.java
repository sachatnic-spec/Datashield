package io.datasheild.breachservice.repository;

import io.datasheild.breachservice.entity.ContainmentAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContainmentActionRepository extends JpaRepository<ContainmentAction, UUID> {

    @Query("SELECT a FROM ContainmentAction a WHERE a.breachIncidentId = :incidentId ORDER BY a.createdAt DESC")
    List<ContainmentAction> findByIncident(@Param("incidentId") UUID incidentId);

    @Query("SELECT a FROM ContainmentAction a WHERE a.breachIncidentId = :incidentId AND a.status = 'IN_PROGRESS'")
    List<ContainmentAction> findOngoingActions(@Param("incidentId") UUID incidentId);

    @Query("SELECT COUNT(a) FROM ContainmentAction a WHERE a.breachIncidentId = :incidentId AND a.status = 'FAILED'")
    long countFailedActions(@Param("incidentId") UUID incidentId);
}
