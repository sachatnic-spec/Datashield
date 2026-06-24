package com.datasheild.configservice.service;

import com.datasheild.configservice.dto.ConfigUpdatedEvent;
import com.datasheild.configservice.dto.TenantConfigRequest;
import com.datasheild.configservice.dto.TenantConfigResponse;
import com.datasheild.configservice.entity.ConfigStatus;
import com.datasheild.configservice.entity.TenantConfig;
import com.datasheild.configservice.exception.ResourceNotFoundException;
import com.datasheild.configservice.repository.TenantConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfigService {

    private final TenantConfigRepository tenantConfigRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.config-topic:config.updated}")
    private String configUpdatedTopic;

    public TenantConfigResponse getConfig(String tenantId) {
        TenantConfig config = tenantConfigRepository.findByTenantIdAndStatus(tenantId, ConfigStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active config found for tenant " + tenantId));
        return TenantConfigResponse.from(config);
    }

    @Transactional
    public TenantConfigResponse upsertConfig(String tenantId, TenantConfigRequest request) {
        TenantConfig config = tenantConfigRepository.findByTenantId(tenantId)
                .orElseGet(() -> TenantConfig.builder().tenantId(tenantId).build());
        config.setConsentModel(request.consentModel());
        config.setRetentionPeriodDays(request.retentionPeriodDays());
        config.setDpoEmail(request.dpoEmail());
        config.setStatus(ConfigStatus.ACTIVE);
        TenantConfig saved = tenantConfigRepository.save(config);
        kafkaTemplate.send(configUpdatedTopic, tenantId, ConfigUpdatedEvent.from(saved));
        return TenantConfigResponse.from(saved);
    }
}
