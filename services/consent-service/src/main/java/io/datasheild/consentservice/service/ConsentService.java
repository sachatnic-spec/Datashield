package io.datasheild.consentservice.service;

import io.datasheild.consentservice.dto.ConsentResponse;
import io.datasheild.consentservice.dto.GrantConsentRequest;
import io.datasheild.consentservice.dto.PurposeResponse;
import io.datasheild.consentservice.dto.WithdrawConsentRequest;
import io.datasheild.consentservice.entity.ConsentAuditOutbox;
import io.datasheild.consentservice.entity.ConsentRecord;
import io.datasheild.consentservice.entity.ConsentPurpose;
import io.datasheild.consentservice.repository.ConsentAuditOutboxRepository;
import io.datasheild.consentservice.repository.ConsentPurposeRepository;
import io.datasheild.consentservice.repository.ConsentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentService {

    private final ConsentRecordRepository consentRecordRepository;
    private final ConsentPurposeRepository consentPurposeRepository;
    private final ConsentAuditOutboxRepository consentAuditOutboxRepository;

    @Transactional
    public ConsentResponse grantConsent(UUID tenantId, GrantConsentRequest request) {
        log.info("Granting consent for tenant={} dataPrincipal={} purpose={}", tenantId, request.getDataPrincipalId(), request.getPurposeId());

        // Validate purpose exists and is active
        ConsentPurpose purpose = consentPurposeRepository.findById(request.getPurposeId())
                .orElseThrow(() -> new IllegalArgumentException("Purpose not found"));

        if (!purpose.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Purpose does not belong to tenant");
        }

        // Check for existing consent (prevent duplicates within same session)
        var existing = consentRecordRepository.findByTenantAndDPAndPurpose(tenantId, request.getDataPrincipalId(), request.getPurposeId());
        if (existing.isPresent() && existing.get().getStatus() == ConsentRecord.ConsentStatus.GRANTED) {
            log.warn("Consent already exists for this DP and purpose");
            return mapToResponse(existing.get());
        }

        // Create consent record
        ConsentRecord consent = ConsentRecord.builder()
                .tenantId(tenantId)
                .dataPrincipalId(request.getDataPrincipalId())
                .purposeId(request.getPurposeId())
                .status(ConsentRecord.ConsentStatus.GRANTED)
                .ipAddress(request.getIpAddress())
                .deviceFingerprint(request.getDeviceFingerprint())
                .channel(request.getChannel())
                .metadata(request.getMetadata())
                .expiresAt(computeExpiryDate(purpose.getRetentionDays()))
                .auditLogged(false)
                .build();

        ConsentRecord saved = consentRecordRepository.save(consent);

        // Publish event to outbox (Debezium CDC will pick it up)
        publishEvent(tenantId, "consent.granted", buildEventPayload(saved));

        log.info("Consent granted: id={}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public ConsentResponse withdrawConsent(UUID tenantId, WithdrawConsentRequest request) {
        log.info("Withdrawing consent: id={}", request.getConsentId());

        ConsentRecord consent = consentRecordRepository.findById(request.getConsentId())
                .orElseThrow(() -> new IllegalArgumentException("Consent not found"));

        if (!consent.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Consent does not belong to tenant");
        }

        consent.setStatus(ConsentRecord.ConsentStatus.WITHDRAWN);
        consent.setWithdrawnAt(LocalDateTime.now());
        consent.setWithdrawalReason(request.getReason());

        ConsentRecord updated = consentRecordRepository.save(consent);

        // Publish event
        publishEvent(tenantId, "consent.withdrawn", buildEventPayload(updated));

        log.info("Consent withdrawn: id={}", updated.getId());
        return mapToResponse(updated);
    }

    public List<ConsentResponse> getActiveConsents(UUID tenantId, UUID dataPrincipalId) {
        log.debug("Fetching active consents for tenant={} dataPrincipal={}", tenantId, dataPrincipalId);

        List<ConsentRecord> records = consentRecordRepository.findActiveConsentsByDataPrincipal(tenantId, dataPrincipalId, LocalDateTime.now());

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PurposeResponse> getActivePurposes(UUID tenantId) {
        log.debug("Fetching active purposes for tenant={}", tenantId);

        List<ConsentPurpose> purposes = consentPurposeRepository.findActiveByTenant(tenantId);

        return purposes.stream()
                .map(this::mapPurposeToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void expireOldConsents(UUID tenantId) {
        log.info("Expiring old consents for tenant={}", tenantId);

        List<ConsentRecord> expired = consentRecordRepository.findExpiredConsents(tenantId, LocalDateTime.now());

        for (ConsentRecord record : expired) {
            record.setStatus(ConsentRecord.ConsentStatus.EXPIRED);
            consentRecordRepository.save(record);
            publishEvent(tenantId, "consent.expired", buildEventPayload(record));
        }

        log.info("Expired {} consents", expired.size());
    }

    private void publishEvent(UUID tenantId, String eventType, String payload) {
        ConsentAuditOutbox outbox = ConsentAuditOutbox.builder()
                .tenantId(tenantId)
                .eventType(eventType)
                .eventPayload(payload)
                .published(false)
                .retryCount(0)
                .build();

        consentAuditOutboxRepository.save(outbox);
        log.debug("Event published to outbox: eventType={}", eventType);
    }

    private String buildEventPayload(ConsentRecord record) {
        // Simple JSON payload (use Jackson ObjectMapper in production)
        return "{\"consentId\":\"" + record.getId() + "\",\"purposeId\":\"" + record.getPurposeId() + 
               "\",\"status\":\"" + record.getStatus() + "\"}";
    }

    private LocalDateTime computeExpiryDate(Integer retentionDays) {
        if (retentionDays == null || retentionDays == 0) {
            return null; // No expiry
        }
        return LocalDateTime.now().plusDays(retentionDays);
    }

    private ConsentResponse mapToResponse(ConsentRecord record) {
        return ConsentResponse.builder()
                .id(record.getId())
                .dataPrincipalId(record.getDataPrincipalId())
                .purposeId(record.getPurposeId())
                .status(record.getStatus().toString())
                .grantedAt(record.getGrantedAt())
                .withdrawnAt(record.getWithdrawnAt())
                .expiresAt(record.getExpiresAt())
                .channel(record.getChannel())
                .ipAddress(record.getIpAddress())
                .auditLogged(record.getAuditLogged())
                .withdrawalReason(record.getWithdrawalReason())
                .build();
    }

    private PurposeResponse mapPurposeToResponse(ConsentPurpose purpose) {
        return PurposeResponse.builder()
                .id(purpose.getId())
                .purposeCode(purpose.getPurposeCode())
                .purposeName(purpose.getPurposeName())
                .description(purpose.getDescription())
                .retentionDays(purpose.getRetentionDays())
                .status(purpose.getStatus().toString())
                .requiresAudit(purpose.getRequiresAudit())
                .build();
    }
}
