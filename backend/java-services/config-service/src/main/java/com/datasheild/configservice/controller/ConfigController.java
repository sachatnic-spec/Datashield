package com.datasheild.configservice.controller;

import com.datasheild.configservice.dto.TenantConfigRequest;
import com.datasheild.configservice.dto.TenantConfigResponse;
import com.datasheild.configservice.service.ConfigService;
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
@RequestMapping("/config")
public class ConfigController {

    private final ConfigService configService;

    @GetMapping("/{tenantId}")
    public TenantConfigResponse getConfig(@PathVariable String tenantId) {
        return configService.getConfig(tenantId);
    }

    @PostMapping("/{tenantId}")
    public TenantConfigResponse upsertConfig(@PathVariable String tenantId, @Valid @RequestBody TenantConfigRequest request) {
        return configService.upsertConfig(tenantId, request);
    }
}
