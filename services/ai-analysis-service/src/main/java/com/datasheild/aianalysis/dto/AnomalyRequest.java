package com.datasheild.aianalysis.dto;

import com.datasheild.aianalysis.entity.AnomalyDetection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyRequest {
    private AnomalyDetection.MetricType metricType;
    private Double currentValue;
    private Double[] historicalValues;
}
