package com.datasheild.anomalydetection.controller;

import com.datasheild.anomalydetection.dto.AccessAnomalyRequest;
import com.datasheild.anomalydetection.dto.BehaviorProfileResponse;
import com.datasheild.anomalydetection.entity.BehavioralAnomaly;
import com.datasheild.anomalydetection.service.BehavioralAnomalyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/anomaly-detection")
@RequiredArgsConstructor
@Tag(name = "Anomaly Detection", description = "Behavioral access anomaly endpoints")
public class AnomalyDetectionController {
    private final BehavioralAnomalyService behavioralAnomalyService;

    @PostMapping("/detect-access-anomaly")
    @Operation(summary = "Detect access anomalies based on user behavior baselines")
    public ResponseEntity<BehavioralAnomaly> detectAccessAnomaly(@Valid @RequestBody AccessAnomalyRequest request) {
        return ResponseEntity.ok(behavioralAnomalyService.detectAccessAnomaly(
            request.getUserId(),
            request.getAccessTime(),
            request.getLocation(),
            request.getVolume()
        ));
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Generate a 30-day behavioral profile for a user")
    public ResponseEntity<BehaviorProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(behavioralAnomalyService.generateBehaviorProfile(userId));
    }

    @PostMapping("/flag-unauthorized")
    @Operation(summary = "Flag unauthorized access when risk exceeds a threshold")
    public ResponseEntity<BehavioralAnomaly> flagUnauthorized(@RequestParam UUID userId,
                                                              @RequestParam double risk) {
        return ResponseEntity.ok(behavioralAnomalyService.flagUnauthorizedAccess(userId, risk));
    }
}
