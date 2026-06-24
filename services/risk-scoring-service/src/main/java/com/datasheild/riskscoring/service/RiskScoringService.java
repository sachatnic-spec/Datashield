package com.datasheild.riskscoring.service;

import com.datasheild.riskscoring.dto.RiskScoreRequest;
import com.datasheild.riskscoring.dto.VendorRiskResponse;
import com.datasheild.riskscoring.entity.RiskScore;
import com.datasheild.riskscoring.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskScoringService {
    private final RiskScoreRepository repository;

    public VendorRiskResponse scoreVendor(UUID vendorId, RiskScoreRequest factors) {
        double security = normalize(factors.getSecurity());
        double compliance = normalize(factors.getCompliance());
        double operational = normalize(factors.getOperational());
        double historical = normalize(factors.getHistorical());
        double score = (security * 0.4) + (compliance * 0.35) + (operational * 0.15) + (historical * 0.1);
        RiskScore.TrendDirection trend = determineTrend(vendorId, score);

        RiskScore riskScore = RiskScore.builder()
            .tenantId(factors.getTenantId())
            .vendorId(vendorId)
            .securityFactor(security)
            .complianceFactor(compliance)
            .operationalFactor(operational)
            .historicalFactor(historical)
            .overallRiskScore(round(score))
            .riskLevel(determineLevel(score))
            .trendDirection(trend)
            .rationale(buildRationale(security, compliance, operational, historical, score))
            .recommendedAction(buildRecommendation(score, compliance, security))
            .scoredAt(LocalDateTime.now())
            .build();

        return toResponse(repository.save(riskScore));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> predictRiskTrend(UUID vendorId, List<Double> historicalScores) {
        List<Double> scores = historicalScores == null || historicalScores.isEmpty()
            ? repository.findByVendorId(vendorId).stream().map(RiskScore::getOverallRiskScore).toList()
            : historicalScores;
        if (scores.isEmpty()) {
            return Map.of(
                "vendorId", vendorId,
                "trend", RiskScore.TrendDirection.STABLE,
                "projectedRisk", 0.0,
                "message", "No historical scores available"
            );
        }

        double averageDelta = calculateAverageDelta(scores);
        double projectedRisk = round(clamp(scores.get(scores.size() - 1) + averageDelta));
        RiskScore.TrendDirection trend = classifyTrend(averageDelta);
        double volatility = round(calculateVolatility(scores));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("vendorId", vendorId);
        response.put("trend", trend);
        response.put("currentRisk", round(scores.get(scores.size() - 1)));
        response.put("averageDelta", round(averageDelta));
        response.put("projectedRisk", projectedRisk);
        response.put("volatility", volatility);
        response.put("observations", scores.size());
        return response;
    }

    @Transactional(readOnly = true)
    public List<VendorRiskResponse> getTopRisks(UUID tenantId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return repository.findTopRisks(tenantId, PageRequest.of(0, safeLimit)).stream()
            .map(this::toResponse)
            .toList();
    }

    private RiskScore.TrendDirection determineTrend(UUID vendorId, double newScore) {
        return repository.findTopByVendorIdOrderByScoredAtDesc(vendorId)
            .map(existing -> classifyTrend(newScore - existing.getOverallRiskScore()))
            .orElse(RiskScore.TrendDirection.STABLE);
    }

    private RiskScore.RiskLevel determineLevel(double score) {
        if (score >= 0.80) {
            return RiskScore.RiskLevel.CRITICAL;
        }
        if (score >= 0.65) {
            return RiskScore.RiskLevel.HIGH;
        }
        if (score >= 0.40) {
            return RiskScore.RiskLevel.MEDIUM;
        }
        return RiskScore.RiskLevel.LOW;
    }

    private RiskScore.TrendDirection classifyTrend(double delta) {
        if (delta >= 0.03) {
            return RiskScore.TrendDirection.RISING;
        }
        if (delta <= -0.03) {
            return RiskScore.TrendDirection.FALLING;
        }
        return RiskScore.TrendDirection.STABLE;
    }

    private double calculateAverageDelta(List<Double> scores) {
        if (scores.size() < 2) {
            return 0.0;
        }
        double deltaSum = 0.0;
        for (int index = 1; index < scores.size(); index++) {
            deltaSum += scores.get(index) - scores.get(index - 1);
        }
        return deltaSum / (scores.size() - 1);
    }

    private double calculateVolatility(List<Double> scores) {
        if (scores.size() < 2) {
            return 0.0;
        }
        double averageDelta = calculateAverageDelta(scores);
        double totalVariance = 0.0;
        for (int index = 1; index < scores.size(); index++) {
            double delta = scores.get(index) - scores.get(index - 1);
            totalVariance += Math.pow(delta - averageDelta, 2);
        }
        return Math.sqrt(totalVariance / (scores.size() - 1));
    }

    private VendorRiskResponse toResponse(RiskScore entity) {
        return VendorRiskResponse.builder()
            .tenantId(entity.getTenantId())
            .vendorId(entity.getVendorId())
            .overallRiskScore(entity.getOverallRiskScore())
            .riskLevel(entity.getRiskLevel())
            .trendDirection(entity.getTrendDirection())
            .securityFactor(entity.getSecurityFactor())
            .complianceFactor(entity.getComplianceFactor())
            .operationalFactor(entity.getOperationalFactor())
            .historicalFactor(entity.getHistoricalFactor())
            .rationale(entity.getRationale())
            .recommendedAction(entity.getRecommendedAction())
            .scoredAt(entity.getScoredAt())
            .build();
    }

    private String buildRationale(double security, double compliance, double operational, double historical, double score) {
        return String.format(
            "Weighted score %.2f derived from security %.2f, compliance %.2f, operational %.2f, historical %.2f.",
            round(score), round(security), round(compliance), round(operational), round(historical)
        );
    }

    private String buildRecommendation(double score, double compliance, double security) {
        if (score >= 0.80) {
            return "Escalate immediately, pause high-risk data sharing, and trigger senior vendor review.";
        }
        if (compliance >= 0.75 || security >= 0.75) {
            return "Schedule remediation checkpoints with the vendor and review control attestations.";
        }
        if (score >= 0.40) {
            return "Maintain enhanced monitoring and refresh assessment evidence in the next cycle.";
        }
        return "Continue standard monitoring cadence with periodic reassessment.";
    }

    private double normalize(Double value) {
        return clamp(value == null ? 0.0 : value);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
