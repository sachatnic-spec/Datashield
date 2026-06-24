package com.datasheild.aianalysis.dto;

import com.datasheild.aianalysis.entity.TrendForecast;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastRequest {
    private TrendForecast.MetricType metricType;
    private Double[] historicalValues;
    private Integer forecastDays;
}
