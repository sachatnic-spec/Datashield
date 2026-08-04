package io.datasheild.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> fallback() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/**")
    public ResponseEntity<Map<String, Object>> genericFallback() {
        return fallback();
    }

}
