package com.datasheild.configservice.dto;

import java.time.LocalDateTime;

public record SecretResponse(String key, String secretValue, Integer keyVersion, LocalDateTime updatedAt) {
}
