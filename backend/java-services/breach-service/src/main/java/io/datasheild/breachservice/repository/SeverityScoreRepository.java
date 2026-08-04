package io.datasheild.breachservice.repository;

import io.datasheild.breachservice.entity.SeverityScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeverityScoreRepository extends JpaRepository<SeverityScore, UUID> {

    @Query("SELECT s FROM SeverityScore s WHERE s.breachIncidentId = :incidentId ORDER BY s.calculatedAt DESC LIMIT 1")
    Optional<SeverityScore> findLatestByIncident(@Param("incidentId") UUID incidentId);

    @Query("SELECT s FROM SeverityScore s WHERE s.breachIncidentId = :incidentId AND s.reviewedAt IS NOT NULL")
    Optional<SeverityScore> findReviewedScore(@Param("incidentId") UUID incidentId);
}
