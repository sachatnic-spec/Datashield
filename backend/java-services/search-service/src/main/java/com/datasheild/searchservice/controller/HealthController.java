package com.datasheild.searchservice.controller;

import com.datasheild.searchservice.service.ElasticsearchService;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final ElasticsearchService elasticsearchService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean elasticsearchUp = elasticsearchService.isRemoteAvailable();
        return ResponseEntity.ok(Map.of(
                "status", elasticsearchUp ? "UP" : "DEGRADED",
                "service", "search-service",
                "elasticsearch", elasticsearchUp ? "UP" : "DOWN",
                "timestamp", Instant.now().toString()));
    }
}
