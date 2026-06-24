package com.datasheild.searchservice.dto;

import java.util.List;
import java.util.Map;

public record SearchExecutionResult(List<Map<String, Object>> results, long totalHits, boolean degraded) {
}
