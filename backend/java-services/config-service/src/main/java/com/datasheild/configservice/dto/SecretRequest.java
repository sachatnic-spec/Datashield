package com.datasheild.configservice.dto;

import jakarta.validation.constraints.NotBlank;

public record SecretRequest(@NotBlank String key, @NotBlank String secretValue) {
}
