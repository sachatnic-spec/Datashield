package com.datasheild.searchservice.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SearchResultResponse(UUID queryId, String tenantId, String status, boolean degraded, long totalHits,
                                   Instant createdAt, Instant completedAt, List<Map<String, Object>> results) {
}
