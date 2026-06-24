package io.datasheild.tenantservice.service;

import io.datasheild.tenantservice.entity.FeatureFlag;
import io.datasheild.tenantservice.entity.Tenant;
import io.datasheild.tenantservice.exception.TenantException;
import io.datasheild.tenantservice.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    @Transactional
    public FeatureFlag createFeatureFlag(String flagName, Tenant.TenantTier tier, Boolean isActive, String description) {
        log.info("Creating feature flag: {} for tier: {}", flagName, tier);

        FeatureFlag flag = FeatureFlag.createForTier(tier, flagName, isActive);
        flag.setDescription(description);
        flag = featureFlagRepository.save(flag);

        log.info("Feature flag created: {} (ID: {})", flagName, flag.getId());
        return flag;
    }

    @Transactional
    public FeatureFlag updateFeatureFlag(UUID flagId, Boolean isActive, String description, String featureValue) {
        log.info("Updating feature flag: {}", flagId);

        FeatureFlag flag = featureFlagRepository.findById(flagId)
            .orElseThrow(() -> new TenantException.FeatureFlagNotFoundException(flagId.toString()));

        if (isActive != null) {
            flag.setIsActive(isActive);
        }

        if (description != null) {
            flag.setDescription(description);
        }

        if (featureValue != null) {
            flag.setFeatureValue(featureValue);
        }

        flag = featureFlagRepository.save(flag);
        log.info("Feature flag updated: {}", flagId);

        return flag;
    }

    @Transactional(readOnly = true)
    public FeatureFlag getFeatureFlag(String flagName, Tenant.TenantTier tier) {
        return featureFlagRepository.findByFlagNameAndTier(flagName, tier)
            .orElseThrow(() -> new TenantException.FeatureFlagNotFoundException(flagName + " for tier " + tier));
    }

    @Transactional(readOnly = true)
    public List<FeatureFlag> getActiveFeaturesByTier(Tenant.TenantTier tier) {
        log.debug("Fetching active features for tier: {}", tier);
        return featureFlagRepository.findActiveFeaturesByTier(tier);
    }

    @Transactional(readOnly = true)
    public List<FeatureFlag> getAllFeaturesByTier(Tenant.TenantTier tier) {
        log.debug("Fetching all features for tier: {}", tier);
        return featureFlagRepository.findAllFeaturesByTier(tier);
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabledForTier(String flagName, Tenant.TenantTier tier) {
        boolean enabled = featureFlagRepository.isFeatureEnabledForTier(flagName, tier);
        log.debug("Feature {} enabled for tier {}: {}", flagName, tier, enabled);
        return enabled;
    }

    @Transactional(readOnly = true)
    public Long countActiveFeatures(Tenant.TenantTier tier) {
        return featureFlagRepository.countActiveByTier(tier);
    }

    @Transactional
    public void initializeTierFeatures(Tenant.TenantTier tier) {
        log.info("Initializing features for tier: {}", tier);

        // Common features for all tiers
        createOrUpdateFeature("consent_management", tier, true, "Basic consent management");
        createOrUpdateFeature("audit_logging", tier, true, "Audit trail functionality");

        // Tier-specific features
        switch (tier) {
            case STARTER:
                createOrUpdateFeature("basic_analytics", tier, true, "Basic analytics dashboard");
                createOrUpdateFeature("single_schema", tier, true, "Single schema per tenant");
                createOrUpdateFeature("email_notifications", tier, true, "Email-only notifications");
                createOrUpdateFeature("standard_sla", tier, true, "Standard SLA (24 hours)");
                break;

            case PROFESSIONAL:
                createOrUpdateFeature("advanced_analytics", tier, true, "Advanced analytics");
                createOrUpdateFeature("multi_schema", tier, true, "Multiple schemas");
                createOrUpdateFeature("multi_channel_notifications", tier, true, "Email, SMS, Push");
                createOrUpdateFeature("premium_sla", tier, true, "Premium SLA (4 hours)");
                createOrUpdateFeature("api_webhooks", tier, true, "Webhook integrations");
                break;

            case ENTERPRISE:
                createOrUpdateFeature("custom_analytics", tier, true, "Custom analytics");
                createOrUpdateFeature("dedicated_schema", tier, true, "Dedicated database");
                createOrUpdateFeature("white_label", tier, true, "White-labeling");
                createOrUpdateFeature("sso_saml", tier, true, "SSO/SAML integration");
                createOrUpdateFeature("priority_sla", tier, true, "Priority SLA (1 hour)");
                createOrUpdateFeature("custom_integrations", tier, true, "Custom integrations");
                break;

            case GOVERNMENT:
                createOrUpdateFeature("government_compliance", tier, true, "Government compliance");
                createOrUpdateFeature("dedicated_infrastructure", tier, true, "Dedicated infrastructure");
                createOrUpdateFeature("air_gapped_deployment", tier, true, "Air-gapped deployment");
                createOrUpdateFeature("top_priority_sla", tier, true, "Top priority SLA (15 mins)");
                createOrUpdateFeature("on_premise_option", tier, true, "On-premise deployment");
                createOrUpdateFeature("data_residency_control", tier, true, "Full data residency control");
                break;
        }

        log.info("Features initialized for tier: {}", tier);
    }

    private void createOrUpdateFeature(String flagName, Tenant.TenantTier tier, Boolean enabled, String description) {
        try {
            FeatureFlag existing = featureFlagRepository.findByFlagNameAndTier(flagName, tier)
                .orElse(null);

            if (existing == null) {
                createFeatureFlag(flagName, tier, enabled, description);
            }
        } catch (Exception ex) {
            log.warn("Could not initialize feature flag: {} for tier: {}", flagName, tier, ex);
        }
    }
}
