package com.datasheild.anomalydetection.service;

import com.datasheild.anomalydetection.dto.BehaviorProfileResponse;
import com.datasheild.anomalydetection.entity.BehavioralAnomaly;
import com.datasheild.anomalydetection.repository.BehavioralAnomalyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BehavioralAnomalyService {
    private static final int PROFILE_WINDOW_DAYS = 30;

    private final BehavioralAnomalyRepository repository;

    public BehavioralAnomaly detectAccessAnomaly(UUID userId, LocalDateTime accessTime, String location, Integer volume) {
        LocalDateTime effectiveAccessTime = accessTime == null ? LocalDateTime.now() : accessTime;
        int safeVolume = volume == null ? 1 : Math.max(1, volume);
        BehaviorProfile profile = buildProfile(userId);

        double timeDeviation = calculateTimeDeviation(profile, effectiveAccessTime);
        double volumeDeviation = calculateVolumeDeviation(profile, safeVolume);
        double geographyDeviation = calculateGeographyDeviation(profile, location);
        double riskScore = round(Math.min(1.0, (timeDeviation * 0.35) + (volumeDeviation * 0.35) + (geographyDeviation * 0.30)));

        BehavioralAnomaly anomaly = BehavioralAnomaly.builder()
            .userId(userId)
            .accessTime(effectiveAccessTime)
            .location(location)
            .volume(safeVolume)
            .timeDeviationScore(timeDeviation)
            .volumeDeviationScore(volumeDeviation)
            .geographyDeviationScore(geographyDeviation)
            .overallRiskScore(riskScore)
            .severity(determineSeverity(riskScore))
            .unauthorizedAccess(riskScore >= 0.70)
            .criticalAlert(riskScore > 0.80)
            .baselineWindowDays(PROFILE_WINDOW_DAYS)
            .profileSnapshot(snapshot(profile))
            .explanation(buildExplanation(location, effectiveAccessTime, safeVolume, timeDeviation, volumeDeviation, geographyDeviation, riskScore))
            .build();

        return repository.save(anomaly);
    }

    @Transactional(readOnly = true)
    public BehaviorProfileResponse generateBehaviorProfile(UUID userId) {
        BehaviorProfile profile = buildProfile(userId);
        return BehaviorProfileResponse.builder()
            .userId(userId)
            .generatedAt(LocalDateTime.now())
            .sampleSize(profile.sampleSize())
            .baselineAccessHour(round(profile.averageHour()))
            .averageVolume(round(profile.averageVolume()))
            .frequentLocations(profile.frequentLocations())
            .averageRiskScore(round(profile.averageRiskScore()))
            .normalBusinessHours(String.format("%.0f:00-%.0f:00", Math.max(0.0, profile.averageHour() - 2), Math.min(23.0, profile.averageHour() + 2)))
            .profileSummary(buildProfileSummary(profile))
            .build();
    }

    public BehavioralAnomaly flagUnauthorizedAccess(UUID userId, double risk) {
        BehaviorProfile profile = buildProfile(userId);
        double clampedRisk = round(Math.max(0.0, Math.min(1.0, risk)));
        BehavioralAnomaly anomaly = repository.findTopByUserIdOrderByDetectedAtDesc(userId)
            .map(existing -> BehavioralAnomaly.builder()
                .userId(userId)
                .accessTime(LocalDateTime.now())
                .location(existing.getLocation())
                .volume(existing.getVolume())
                .timeDeviationScore(existing.getTimeDeviationScore())
                .volumeDeviationScore(existing.getVolumeDeviationScore())
                .geographyDeviationScore(existing.getGeographyDeviationScore())
                .overallRiskScore(clampedRisk)
                .severity(determineSeverity(clampedRisk))
                .unauthorizedAccess(clampedRisk >= 0.70)
                .criticalAlert(clampedRisk > 0.80)
                .baselineWindowDays(PROFILE_WINDOW_DAYS)
                .profileSnapshot(snapshot(profile))
                .explanation("Unauthorized access manually flagged by risk threshold evaluation")
                .build())
            .orElseGet(() -> BehavioralAnomaly.builder()
                .userId(userId)
                .accessTime(LocalDateTime.now())
                .location("UNKNOWN")
                .volume(1)
                .timeDeviationScore(clampedRisk)
                .volumeDeviationScore(clampedRisk)
                .geographyDeviationScore(clampedRisk)
                .overallRiskScore(clampedRisk)
                .severity(determineSeverity(clampedRisk))
                .unauthorizedAccess(clampedRisk >= 0.70)
                .criticalAlert(clampedRisk > 0.80)
                .baselineWindowDays(PROFILE_WINDOW_DAYS)
                .profileSnapshot(snapshot(profile))
                .explanation("Unauthorized access flagged without historical access sample")
                .build());
        return repository.save(anomaly);
    }

    private BehaviorProfile buildProfile(UUID userId) {
        List<BehavioralAnomaly> history = repository.findRecentByUserId(userId, LocalDateTime.now().minusDays(PROFILE_WINDOW_DAYS));
        if (history.isEmpty()) {
            return new BehaviorProfile(9.0, 10.0, 0.25, List.of(), 0);
        }

        double averageHour = history.stream().mapToDouble(item -> item.getAccessTime().getHour()).average().orElse(9.0);
        double averageVolume = history.stream().mapToInt(BehavioralAnomaly::getVolume).average().orElse(10.0);
        double averageRisk = history.stream().mapToDouble(BehavioralAnomaly::getOverallRiskScore).average().orElse(0.25);
        List<String> frequentLocations = history.stream()
            .collect(Collectors.groupingBy(BehavioralAnomaly::getLocation, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(3)
            .map(Map.Entry::getKey)
            .toList();

        return new BehaviorProfile(averageHour, averageVolume, averageRisk, frequentLocations, history.size());
    }

    private double calculateTimeDeviation(BehaviorProfile profile, LocalDateTime accessTime) {
        double difference = Math.abs(accessTime.getHour() - profile.averageHour());
        double normalized = Math.min(1.0, difference / 12.0);
        if (accessTime.getHour() < 6 || accessTime.getHour() > 22) {
            normalized = Math.min(1.0, normalized + 0.20);
        }
        return round(normalized);
    }

    private double calculateVolumeDeviation(BehaviorProfile profile, int volume) {
        double baseline = Math.max(1.0, profile.averageVolume());
        double normalized = Math.min(1.0, Math.abs(volume - baseline) / baseline);
        return round(normalized);
    }

    private double calculateGeographyDeviation(BehaviorProfile profile, String location) {
        if (profile.frequentLocations().isEmpty()) {
            return 0.35;
        }
        return profile.frequentLocations().stream().anyMatch(saved -> saved.equalsIgnoreCase(location)) ? 0.10 : 0.85;
    }

    private BehavioralAnomaly.Severity determineSeverity(double riskScore) {
        if (riskScore > 0.80) {
            return BehavioralAnomaly.Severity.CRITICAL;
        }
        if (riskScore >= 0.65) {
            return BehavioralAnomaly.Severity.HIGH;
        }
        if (riskScore >= 0.40) {
            return BehavioralAnomaly.Severity.MODERATE;
        }
        return BehavioralAnomaly.Severity.LOW;
    }

    private String buildExplanation(String location, LocalDateTime accessTime, int volume, double timeDeviation,
                                    double volumeDeviation, double geographyDeviation, double riskScore) {
        return String.format(
            "Access at %s from %s with volume %d produced time deviation %.2f, volume deviation %.2f, geography deviation %.2f and risk %.2f.",
            accessTime, location, volume, timeDeviation, volumeDeviation, geographyDeviation, riskScore
        );
    }

    private String buildProfileSummary(BehaviorProfile profile) {
        return String.format(
            "Baseline built from %d events with average hour %.1f, average volume %.1f, and preferred locations %s.",
            profile.sampleSize(), profile.averageHour(), profile.averageVolume(), profile.frequentLocations()
        );
    }

    private String snapshot(BehaviorProfile profile) {
        return String.format(
            "hour=%.2f, volume=%.2f, risk=%.2f, locations=%s",
            profile.averageHour(), profile.averageVolume(), profile.averageRiskScore(), profile.frequentLocations()
        );
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private record BehaviorProfile(double averageHour, double averageVolume, double averageRiskScore,
                                   List<String> frequentLocations, int sampleSize) {
    }
}
