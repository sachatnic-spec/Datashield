package com.datasheild.searchservice.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SearchQueryResponse(UUID queryId, String status, long totalHits, boolean degraded,
                                  List<Map<String, Object>> results) {
}
