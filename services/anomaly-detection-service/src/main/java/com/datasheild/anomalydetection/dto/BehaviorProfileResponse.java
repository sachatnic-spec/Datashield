package com.datasheild.anomalydetection.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BehaviorProfileResponse {
    private UUID userId;
    private LocalDateTime generatedAt;
    private int sampleSize;
    private double baselineAccessHour;
    private double averageVolume;
    private List<String> frequentLocations;
    private double averageRiskScore;
    private String normalBusinessHours;
    private String profileSummary;
}
