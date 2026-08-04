package com.datasheild.searchservice.dto;

import java.util.Map;

public record AggregationResponse(String metricType, String tenantId, boolean degraded, Map<String, Object> metrics) {
}
