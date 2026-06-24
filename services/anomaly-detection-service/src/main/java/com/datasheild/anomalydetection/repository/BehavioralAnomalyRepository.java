package com.datasheild.anomalydetection.repository;

import com.datasheild.anomalydetection.entity.BehavioralAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BehavioralAnomalyRepository extends JpaRepository<BehavioralAnomaly, UUID> {

    @Query("SELECT b FROM BehavioralAnomaly b WHERE b.userId = :userId ORDER BY b.detectedAt DESC")
    List<BehavioralAnomaly> findByUserId(UUID userId);

    @Query("SELECT b FROM BehavioralAnomaly b WHERE b.userId = :userId AND b.accessTime >= :since ORDER BY b.accessTime DESC")
    List<BehavioralAnomaly> findRecentByUserId(UUID userId, LocalDateTime since);

    Optional<BehavioralAnomaly> findTopByUserIdOrderByDetectedAtDesc(UUID userId);
}
