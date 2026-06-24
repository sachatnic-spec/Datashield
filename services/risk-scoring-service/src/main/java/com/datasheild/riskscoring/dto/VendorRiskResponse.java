package com.datasheild.riskscoring.dto;

import com.datasheild.riskscoring.entity.RiskScore;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class VendorRiskResponse {
    private UUID tenantId;
    private UUID vendorId;
    private Double overallRiskScore;
    private RiskScore.RiskLevel riskLevel;
    private RiskScore.TrendDirection trendDirection;
    private Double securityFactor;
    private Double complianceFactor;
    private Double operationalFactor;
    private Double historicalFactor;
    private String rationale;
    private String recommendedAction;
    private LocalDateTime scoredAt;
}
