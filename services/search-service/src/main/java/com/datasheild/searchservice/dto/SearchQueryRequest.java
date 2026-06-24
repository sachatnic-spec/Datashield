package com.datasheild.searchservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record SearchQueryRequest(
        @NotBlank(message = "tenantId is required") String tenantId,
        String indexName,
        String searchTerm,
        String metricType,
        Map<String, String> filters,
        @Min(value = 0, message = "page must be >= 0") Integer page,
        @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") Integer size) {

    public int resolvedPage() {
        return page == null ? 0 : page;
    }

    public int resolvedSize() {
        return size == null ? 20 : size;
    }
}
