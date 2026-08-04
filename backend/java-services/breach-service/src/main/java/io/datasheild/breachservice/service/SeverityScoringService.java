package io.datasheild.breachservice.service;

import io.datasheild.breachservice.entity.BreachIncident;
import io.datasheild.breachservice.entity.SeverityScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class SeverityScoringService {

    public SeverityScore calculateSeverity(UUID tenantId, BreachIncident incident) {
        log.info("Calculating severity score for incident: id={}", incident.getId());

        int baseScore = 0;
        int adjustedScore = 0;

        // Base scoring (0-100)
        // 1. Number of affected data subjects (40 points max)
        if (incident.getEstimatedDataSubjects() != null) {
            if (incident.getEstimatedDataSubjects() > 10000) {
                baseScore += 40;
            } else if (incident.getEstimatedDataSubjects() > 1000) {
                baseScore += 30;
            } else if (incident.getEstimatedDataSubjects() > 100) {
                baseScore += 20;
            } else {
                baseScore += 10;
            }
        }

        // 2. Type of data lost (30 points max)
        if (incident.getLossOfConfidentiality() != null && incident.getLossOfConfidentiality()) {
            baseScore += 15;
        }
        if (incident.getLossOfIntegrity() != null && incident.getLossOfIntegrity()) {
            baseScore += 8;
        }
        if (incident.getLossOfAvailability() != null && incident.getLossOfAvailability()) {
            baseScore += 7;
        }

        // 3. Sensitivity of data (20 points max)
        String dataCategories = incident.getDataCategories();
        if (dataCategories != null) {
            if (dataCategories.contains("health") || dataCategories.contains("biometric") || 
                dataCategories.contains("financial")) {
                baseScore += 20;
            } else if (dataCategories.contains("contact") || dataCategories.contains("location")) {
                baseScore += 10;
            }
        }

        adjustedScore = Math.min(baseScore, 100);

        // Determine severity level
        BreachIncident.SeverityLevel severity;
        if (adjustedScore >= 80) {
            severity = BreachIncident.SeverityLevel.P0;
        } else if (adjustedScore >= 60) {
            severity = BreachIncident.SeverityLevel.P1;
        } else if (adjustedScore >= 40) {
            severity = BreachIncident.SeverityLevel.P2;
        } else {
            severity = BreachIncident.SeverityLevel.P3;
        }

        String rationale = String.format(
            "Base: %d (Subjects: %d, Data types: %d, Sensitivity: %d). Adjusted: %d. Severity: %s",
            baseScore, 
            incident.getEstimatedDataSubjects() != null ? incident.getEstimatedDataSubjects() : 0,
            countDataTypes(incident),
            countSensitivity(incident),
            adjustedScore,
            severity
        );

        return SeverityScore.builder()
                .tenantId(tenantId)
                .breachIncidentId(incident.getId())
                .affectedDataSubjects(incident.getEstimatedDataSubjects() != null ? incident.getEstimatedDataSubjects() : 0)
                .affectedRecords(incident.getEstimatedRecords() != null ? incident.getEstimatedRecords() : 0)
                .sensitiveDataInvolved(hasSensitiveData(incident))
                .highRiskDataSubjects(false)
                .baseScore(baseScore)
                .adjustedScore(adjustedScore)
                .proposedSeverity(severity)
                .scoringRationale(rationale)
                .build();
    }

    private int countDataTypes(BreachIncident incident) {
        if (incident.getLossOfConfidentiality() != null && incident.getLossOfConfidentiality()) return 1;
        if (incident.getLossOfIntegrity() != null && incident.getLossOfIntegrity()) return 1;
        if (incident.getLossOfAvailability() != null && incident.getLossOfAvailability()) return 1;
        return 0;
    }

    private int countSensitivity(BreachIncident incident) {
        String dataCategories = incident.getDataCategories();
        if (dataCategories == null) return 0;
        if (dataCategories.contains("health") || dataCategories.contains("biometric")) return 2;
        if (dataCategories.contains("financial")) return 2;
        return 1;
    }

    private boolean hasSensitiveData(BreachIncident incident) {
        String dataCategories = incident.getDataCategories();
        if (dataCategories == null) return false;
        return dataCategories.contains("health") || dataCategories.contains("biometric") || 
               dataCategories.contains("financial") || dataCategories.contains("children");
    }
}
