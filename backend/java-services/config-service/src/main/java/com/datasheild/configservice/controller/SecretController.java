package com.datasheild.configservice.controller;

import com.datasheild.configservice.dto.SecretRequest;
import com.datasheild.configservice.dto.SecretResponse;
import com.datasheild.configservice.service.SecretVaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/secrets")
public class SecretController {

    private final SecretVaultService secretVaultService;

    @GetMapping("/{key}")
    public SecretResponse getSecret(@PathVariable String key) {
        return secretVaultService.getSecret(key);
    }

    @PostMapping
    public SecretResponse storeSecret(@Valid @RequestBody SecretRequest request) {
        return secretVaultService.storeSecret(request);
    }
}
