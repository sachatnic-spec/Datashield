package com.datasheild.aianalysis.service;

import com.datasheild.aianalysis.entity.AnomalyDetection;
import com.datasheild.aianalysis.entity.TrendForecast;
import com.datasheild.aianalysis.repository.AnomalyDetectionRepository;
import com.datasheild.aianalysis.repository.TrendForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AIAnalysisService {
    private final AnomalyDetectionRepository anomalyRepository;
    private final TrendForecastRepository forecastRepository;

    public AnomalyDetection detectAnomaly(UUID tenantId, AnomalyDetection.MetricType metricType,
                                         Double currentValue, Double[] historicalValues) {
        double baseline = calculateBaseline(historicalValues);
        double deviation = ((currentValue - baseline) / baseline) * 100;
        double anomalyScore = calculateAnomalyScore(currentValue, historicalValues);
        AnomalyDetection.AnomalySeverity severity = determineSeverity(anomalyScore, deviation);

        AnomalyDetection anomaly = AnomalyDetection.builder()
            .tenantId(tenantId)
            .metricType(metricType)
            .metricValue(currentValue)
            .baselineValue(baseline)
            .deviationPercentage(deviation)
            .anomalyScore(anomalyScore)
            .severity(severity)
            .detectionMethod(AnomalyDetection.DetectionMethod.STANDARD_DEVIATION)
            .isAlertTriggered(anomalyScore > 0.75)
            .description(generateAnomalyDescription(metricType, deviation, severity))
            .build();

        return anomalyRepository.save(anomaly);
    }

    private double calculateBaseline(Double[] values) {
        if (values == null || values.length == 0) return 0;
        return Arrays.stream(values).mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double calculateAnomalyScore(Double currentValue, Double[] historicalValues) {
        if (historicalValues == null || historicalValues.length < 2) return 0;
        
        double mean = calculateBaseline(historicalValues);
        double variance = Arrays.stream(historicalValues)
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        
        double stdDev = Math.sqrt(variance);
        if (stdDev == 0) return 0;
        
        double zScore = Math.abs((currentValue - mean) / stdDev);
        return Math.min(zScore / 3.0, 1.0); // Normalize to 0-1
    }

    private AnomalyDetection.AnomalySeverity determineSeverity(double anomalyScore, double deviationPercent) {
        if (anomalyScore > 0.9 || Math.abs(deviationPercent) > 80) {
            return AnomalyDetection.AnomalySeverity.CRITICAL;
        }
        if (anomalyScore > 0.75 || Math.abs(deviationPercent) > 50) {
            return AnomalyDetection.AnomalySeverity.HIGH;
        }
        if (anomalyScore > 0.5 || Math.abs(deviationPercent) > 30) {
            return AnomalyDetection.AnomalySeverity.MEDIUM;
        }
        return AnomalyDetection.AnomalySeverity.LOW;
    }

    private String generateAnomalyDescription(AnomalyDetection.MetricType metricType, 
                                            double deviation, AnomalyDetection.AnomalySeverity severity) {
        String direction = deviation > 0 ? "increased" : "decreased";
        return String.format("%s %s by %.1f%%. Severity: %s", 
            metricType.toString(), direction, Math.abs(deviation), severity);
    }

    public TrendForecast forecastTrend(UUID tenantId, TrendForecast.MetricType metricType,
                                       Double[] historicalValues, Integer forecastDays) {
        double currentValue = historicalValues[historicalValues.length - 1];
        double trend = calculateTrend(historicalValues);
        double predictedValue = currentValue + (trend * forecastDays);
        double confidence = calculateConfidence(historicalValues);

        String forecast = generateForecastDescription(historicalValues, forecastDays, trend);

        TrendForecast forecast_obj = TrendForecast.builder()
            .tenantId(tenantId)
            .metricType(metricType)
            .forecastDays(forecastDays)
            .currentValue(currentValue)
            .predictedValue(predictedValue)
            .confidenceInterval(confidence)
            .forecast30DayTrend(forecast)
            .trendDirection(trend)
            .build();

        return forecastRepository.save(forecast_obj);
    }

    private double calculateTrend(Double[] values) {
        if (values.length < 2) return 0;
        
        int n = values.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values[i];
            sumXY += i * values[i];
            sumX2 += i * i;
        }
        
        // Linear regression slope
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }

    private double calculateConfidence(Double[] values) {
        if (values.length < 3) return 0.5;
        // Simple confidence based on data consistency
        double mean = calculateBaseline(values);
        double variance = Arrays.stream(values)
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        double cv = Math.sqrt(variance) / Math.abs(mean); // Coefficient of variation
        return Math.max(0.5, 1.0 - cv); // Higher consistency = higher confidence
    }

    private String generateForecastDescription(Double[] values, Integer days, double trend) {
        String direction = trend > 0 ? "increasing" : "decreasing";
        return String.format("Trend is %s over next %d days. " +
            "Current: %.2f, Predicted: %.2f", 
            direction, days, values[values.length - 1], 
            values[values.length - 1] + (trend * days));
    }

    public List<AnomalyDetection> getTenantAnomalies(UUID tenantId) {
        return anomalyRepository.findByTenantId(tenantId);
    }

    public List<AnomalyDetection> getCriticalAnomalies(UUID tenantId) {
        return anomalyRepository.findByTenantIdAndSeverity(
            tenantId, AnomalyDetection.AnomalySeverity.CRITICAL);
    }

    public Map<String, Object> generateInsights(UUID tenantId) {
        List<AnomalyDetection> anomalies = anomalyRepository.findByTenantId(tenantId);
        List<TrendForecast> forecasts = forecastRepository.findByTenantId(tenantId);

        long criticalCount = anomalies.stream()
            .filter(a -> a.getSeverity() == AnomalyDetection.AnomalySeverity.CRITICAL).count();
        
        Map<AnomalyDetection.MetricType, Long> anomalyDistribution = anomalies.stream()
            .collect(Collectors.groupingBy(AnomalyDetection::getMetricType, Collectors.counting()));

        double avgConfidence = forecasts.stream()
            .mapToDouble(TrendForecast::getConfidenceInterval)
            .average()
            .orElse(0);

        return Map.of(
            "totalAnomalies", anomalies.size(),
            "criticalAnomalies", criticalCount,
            "anomalyDistribution", anomalyDistribution,
            "activeForecasts", forecasts.size(),
            "avgForecastConfidence", avgConfidence
        );
    }
}
