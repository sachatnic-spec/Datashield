package com.datasheild.aianalysis.controller;

import com.datasheild.aianalysis.dto.AnomalyRequest;
import com.datasheild.aianalysis.dto.ForecastRequest;
import com.datasheild.aianalysis.entity.AnomalyDetection;
import com.datasheild.aianalysis.entity.TrendForecast;
import com.datasheild.aianalysis.service.AIAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai-analysis")
@RequiredArgsConstructor
@Tag(name = "AI Analysis", description = "Anomaly detection and trend forecasting")
public class AIAnalysisController {
    private final AIAnalysisService aiAnalysisService;

    @PostMapping("/anomalies/detect")
    @Operation(summary = "Detect anomalies in metric data")
    public ResponseEntity<AnomalyDetection> detectAnomaly(@RequestHeader("X-Tenant-ID") UUID tenantId,
                                                          @RequestBody AnomalyRequest request) {
        AnomalyDetection anomaly = aiAnalysisService.detectAnomaly(
            tenantId,
            request.getMetricType(),
            request.getCurrentValue(),
            request.getHistoricalValues()
        );
        return ResponseEntity.ok(anomaly);
    }

    @PostMapping("/forecast/trend")
    @Operation(summary = "Generate trend forecast")
    public ResponseEntity<TrendForecast> forecastTrend(@RequestHeader("X-Tenant-ID") UUID tenantId,
                                                       @RequestBody ForecastRequest request) {
        TrendForecast forecast = aiAnalysisService.forecastTrend(
            tenantId,
            request.getMetricType(),
            request.getHistoricalValues(),
            request.getForecastDays()
        );
        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/anomalies/tenant/{tenantId}")
    @Operation(summary = "Get all anomalies for tenant")
    public ResponseEntity<List<AnomalyDetection>> getTenantAnomalies(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(aiAnalysisService.getTenantAnomalies(tenantId));
    }

    @GetMapping("/anomalies/tenant/{tenantId}/critical")
    @Operation(summary = "Get critical anomalies only")
    public ResponseEntity<List<AnomalyDetection>> getCriticalAnomalies(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(aiAnalysisService.getCriticalAnomalies(tenantId));
    }

    @GetMapping("/insights/{tenantId}")
    @Operation(summary = "Generate AI insights and recommendations")
    public ResponseEntity<Map<String, Object>> generateInsights(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(aiAnalysisService.generateInsights(tenantId));
    }
}
