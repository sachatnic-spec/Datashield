package io.datasheild.tenantservice.service;

import io.datasheild.tenantservice.dto.CreateTenantRequest;
import io.datasheild.tenantservice.dto.UpdateTenantRequest;
import io.datasheild.tenantservice.entity.Tenant;
import io.datasheild.tenantservice.entity.TenantProvisioningHistory;
import io.datasheild.tenantservice.entity.TenantProvisioningOutbox;
import io.datasheild.tenantservice.exception.TenantException;
import io.datasheild.tenantservice.repository.TenantProvisioningHistoryRepository;
import io.datasheild.tenantservice.repository.TenantProvisioningOutboxRepository;
import io.datasheild.tenantservice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantProvisioningHistoryRepository provisioningHistoryRepository;
    private final TenantProvisioningOutboxRepository outboxRepository;
    private final SchemaProvisioningService schemaProvisioningService;

    @Value("${tenant.schema.prefix:t_}")
    private String schemaPrefix;

    @Transactional
    public Tenant createTenant(CreateTenantRequest request) {
        log.info("Creating tenant: {}", request.getName());

        // Check if tenant already exists
        if (tenantRepository.findByName(request.getName()).isPresent()) {
            throw new TenantException.TenantAlreadyExistsException(request.getName());
        }

        // Check if schema already exists
        if (tenantRepository.findBySchemaName(request.getSchemaName()).isPresent()) {
            throw new TenantException.SchemaAlreadyExistsException(request.getSchemaName());
        }

        // Create tenant entity
        Tenant tenant = Tenant.builder()
            .name(request.getName())
            .description(request.getDescription())
            .tier(request.getTier())
            .schemaName(request.getSchemaName())
            .subscriptionStatus(Tenant.SubscriptionStatus.ACTIVE)
            .provisioningStatus(Tenant.ProvisioningStatus.PENDING)
            .maxDataPrincipals(request.getMaxDataPrincipals())
            .maxConsents(request.getMaxConsents())
            .maxDPRRequests(request.getMaxDPRRequests())
            .maxStorageGB(request.getMaxStorageGB())
            .apiRateLimitRPM(request.getApiRateLimitRPM())
            .logoUrl(request.getLogoUrl())
            .supportEmail(request.getSupportEmail())
            .supportPhone(request.getSupportPhone())
            .invoiceEmail(request.getInvoiceEmail())
            .contractStartDate(request.getContractStartDate())
            .contractEndDate(request.getContractEndDate())
            .autoRenewal(request.getAutoRenewal())
            .build();

        tenant = tenantRepository.save(tenant);
        log.info("Tenant created: {} (ID: {})", tenant.getName(), tenant.getId());

        // Publish outbox event
        publishTenantEvent(tenant, "tenant.created", "Tenant created");

        return tenant;
    }

    @Transactional
    public Tenant updateTenant(UUID tenantId, UpdateTenantRequest request) {
        log.info("Updating tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantException.TenantNotFoundException(tenantId.toString()));

        if (request.getName() != null && !request.getName().equals(tenant.getName())) {
            if (tenantRepository.findByName(request.getName()).isPresent()) {
                throw new TenantException.TenantAlreadyExistsException(request.getName());
            }
            tenant.setName(request.getName());
        }

        if (request.getDescription() != null) {
            tenant.setDescription(request.getDescription());
        }

        if (request.getSupportEmail() != null) {
            tenant.setSupportEmail(request.getSupportEmail());
        }

        if (request.getSupportPhone() != null) {
            tenant.setSupportPhone(request.getSupportPhone());
        }

        if (request.getInvoiceEmail() != null) {
            tenant.setInvoiceEmail(request.getInvoiceEmail());
        }

        if (request.getContractEndDate() != null) {
            tenant.setContractEndDate(request.getContractEndDate());
        }

        if (request.getAutoRenewal() != null) {
            tenant.setAutoRenewal(request.getAutoRenewal());
        }

        if (request.getLogoUrl() != null) {
            tenant.setLogoUrl(request.getLogoUrl());
        }

        if (request.getMaxDataPrincipals() != null) {
            tenant.setMaxDataPrincipals(request.getMaxDataPrincipals());
        }

        if (request.getMaxConsents() != null) {
            tenant.setMaxConsents(request.getMaxConsents());
        }

        if (request.getMaxDPRRequests() != null) {
            tenant.setMaxDPRRequests(request.getMaxDPRRequests());
        }

        if (request.getMaxStorageGB() != null) {
            tenant.setMaxStorageGB(request.getMaxStorageGB());
        }

        if (request.getApiRateLimitRPM() != null) {
            tenant.setApiRateLimitRPM(request.getApiRateLimitRPM());
        }

        if (request.getSubscriptionStatus() != null) {
            tenant.setSubscriptionStatus(request.getSubscriptionStatus());
        }

        tenant = tenantRepository.save(tenant);
        log.info("Tenant updated: {}", tenantId);

        publishTenantEvent(tenant, "tenant.updated", "Tenant updated");

        return tenant;
    }

    @Transactional
    public void provisionTenant(UUID tenantId, String executedBy) {
        log.info("Starting provisioning for tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantException.TenantNotFoundException(tenantId.toString()));

        if (tenant.getProvisioningStatus() == Tenant.ProvisioningStatus.ACTIVE) {
            log.warn("Tenant already provisioned: {}", tenantId);
            return;
        }

        // Update status to CREATING
        tenant.setProvisioningStatus(Tenant.ProvisioningStatus.CREATING);
        tenantRepository.save(tenant);

        long startTime = System.currentTimeMillis();
        try {
            // Create schema and tables
            schemaProvisioningService.provisionSchema(tenant.getSchemaName());
            log.info("Schema provisioned: {}", tenant.getSchemaName());

            // Create seed data (optional)
            schemaProvisioningService.seedInitialData(tenant.getSchemaName(), tenant.getTier());
            log.info("Seed data loaded for tenant: {}", tenantId);

            // Mark as ACTIVE
            tenant.setProvisioningStatus(Tenant.ProvisioningStatus.ACTIVE);
            tenantRepository.save(tenant);

            long duration = System.currentTimeMillis() - startTime;
            recordProvisioningHistory(tenantId, TenantProvisioningHistory.ProvisioningStatus.SUCCESS, "Provisioning", null, executedBy, duration);
            publishTenantEvent(tenant, "tenant.provisioned", "Tenant provisioned successfully");

            log.info("Tenant provisioned successfully: {} ({}ms)", tenantId, duration);

        } catch (Exception ex) {
            log.error("Provisioning failed for tenant: {}", tenantId, ex);
            
            tenant.setProvisioningStatus(Tenant.ProvisioningStatus.FAILED);
            tenantRepository.save(tenant);

            long duration = System.currentTimeMillis() - startTime;
            recordProvisioningHistory(tenantId, TenantProvisioningHistory.ProvisioningStatus.FAILED, "Provisioning", ex.getMessage(), executedBy, duration);
            publishTenantEvent(tenant, "tenant.provisioning_failed", "Provisioning failed: " + ex.getMessage());

            throw new TenantException.ProvisioningException(tenantId.toString(), ex.getMessage());
        }
    }

    @Transactional
    public void archiveTenant(UUID tenantId, String executedBy) {
        log.info("Archiving tenant: {}", tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantException.TenantNotFoundException(tenantId.toString()));

        if (tenant.getSubscriptionStatus() != Tenant.SubscriptionStatus.ARCHIVED) {
            tenant.setSubscriptionStatus(Tenant.SubscriptionStatus.ARCHIVED);
            tenant.setArchivedAt(LocalDateTime.now());
            tenant.setProvisioningStatus(Tenant.ProvisioningStatus.ARCHIVED);
            tenantRepository.save(tenant);

            recordProvisioningHistory(tenantId, TenantProvisioningHistory.ProvisioningStatus.ARCHIVED, "Archive", null, executedBy, 0L);
            publishTenantEvent(tenant, "tenant.archived", "Tenant archived");

            log.info("Tenant archived: {}", tenantId);
        }
    }

    @Transactional(readOnly = true)
    public Tenant getTenantById(UUID tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantException.TenantNotFoundException(tenantId.toString()));
    }

    @Transactional(readOnly = true)
    public Tenant getTenantByName(String name) {
        return tenantRepository.findByName(name)
            .orElseThrow(() -> new TenantException.TenantNotFoundException(name));
    }

    @Transactional(readOnly = true)
    public Tenant getTenantBySchemaName(String schemaName) {
        return tenantRepository.findBySchemaName(schemaName)
            .orElseThrow(() -> new TenantException.TenantNotFoundException(schemaName));
    }

    @Transactional(readOnly = true)
    public Page<Tenant> getActiveTenants(Pageable pageable) {
        return tenantRepository.findBySubscriptionStatus(Tenant.SubscriptionStatus.ACTIVE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Tenant> getTenantsByTier(Tenant.TenantTier tier, Pageable pageable) {
        return tenantRepository.findByTier(tier, pageable);
    }

    @Transactional(readOnly = true)
    public Long countTenantsByTier(Tenant.TenantTier tier) {
        return tenantRepository.countByTier(tier);
    }

    @Transactional(readOnly = true)
    public List<Tenant> findExpiringContracts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysFrom = now.plusDays(30);
        return tenantRepository.findContractExpiringTenants(now, thirtyDaysFrom);
    }

    @Transactional(readOnly = true)
    public List<Tenant> findExpiredContracts() {
        return tenantRepository.findExpiredContracts(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<TenantProvisioningHistory> getProvisioningHistory(UUID tenantId) {
        return provisioningHistoryRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public Page<TenantProvisioningHistory> getProvisioningHistoryPaginated(UUID tenantId, Pageable pageable) {
        return provisioningHistoryRepository.findByTenantIdPageable(tenantId, pageable);
    }

    private void recordProvisioningHistory(UUID tenantId, TenantProvisioningHistory.ProvisioningStatus status, 
                                          String action, String errorMessage, String executedBy, Long durationMs) {
        TenantProvisioningHistory history = TenantProvisioningHistory.builder()
            .tenantId(tenantId)
            .status(status)
            .action(action)
            .errorMessage(errorMessage)
            .executedBy(executedBy)
            .durationMs(durationMs)
            .build();

        provisioningHistoryRepository.save(history);
        log.debug("Provisioning history recorded: tenant={}, status={}", tenantId, status);
    }

    private void publishTenantEvent(Tenant tenant, String eventType, String details) {
        String eventPayload = String.format(
            "{\"tenant_id\":\"%s\",\"name\":\"%s\",\"tier\":\"%s\",\"schema_name\":\"%s\",\"details\":\"%s\"}",
            tenant.getId(), tenant.getName(), tenant.getTier(), tenant.getSchemaName(), details
        );

        TenantProvisioningOutbox outbox = TenantProvisioningOutbox.builder()
            .tenantId(tenant.getId())
            .eventType(eventType)
            .eventPayload(eventPayload)
            .correlationId(UUID.randomUUID().toString())
            .published(false)
            .retryCount(0)
            .build();

        outboxRepository.save(outbox);
        log.debug("Outbox event published: tenant={}, eventType={}", tenant.getId(), eventType);
    }
}
