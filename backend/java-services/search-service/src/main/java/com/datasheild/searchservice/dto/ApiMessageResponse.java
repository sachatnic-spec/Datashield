package com.datasheild.searchservice.dto;

import java.util.Map;

public record ApiMessageResponse(String message, Map<String, Object> details) {
}
