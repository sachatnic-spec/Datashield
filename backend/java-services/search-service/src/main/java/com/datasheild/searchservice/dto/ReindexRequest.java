package com.datasheild.searchservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ReindexRequest(
        @NotBlank(message = "tenantId is required") String tenantId,
        @NotBlank(message = "sourceIndexName is required") String sourceIndexName,
        @NotBlank(message = "targetIndexName is required") String targetIndexName) {
}
