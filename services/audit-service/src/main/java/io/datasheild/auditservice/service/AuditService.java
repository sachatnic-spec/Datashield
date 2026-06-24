package io.datasheild.auditservice.service;

import io.datasheild.auditservice.entity.AuditEvent;
import io.datasheild.auditservice.entity.AuditHash;
import io.datasheild.auditservice.entity.AuditLog;
import io.datasheild.auditservice.repository.AuditEventRepository;
import io.datasheild.auditservice.repository.AuditHashRepository;
import io.datasheild.auditservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditHashRepository auditHashRepository;


    @Transactional
    public void ingestAuditEvent(AuditEvent event) {
        if (!event.isValid()) {
            throw new IllegalArgumentException("Invalid audit event");
        }

        log.info("Ingesting audit event: correlation={} entity={}", event.getCorrelationId(), event.getEntityType());

        AuditEvent saved = auditEventRepository.save(event);

        // Create audit log
        String summary = String.format("%s:%s:%s", event.getSourceService(), event.getEntityType(), event.getEventType());
        AuditLog auditLog = AuditLog.builder()
                .auditEventId(saved.getId())
                .tenantId(saved.getTenantId())
                .eventSummary(summary)
                .archived(false)
                .build();

        AuditLog savedLog = auditLogRepository.save(auditLog);

        // Create hash chain entry
        String eventHash = computeHash(saved.getEventPayload());
        Optional<AuditHash> latestHash = auditHashRepository.findLatestByLog(savedLog.getId(), saved.getTenantId());

        long sequenceNumber = latestHash.map(h -> h.getSequenceNumber() + 1).orElse(1L);
        String previousHash = latestHash.map(AuditHash::getChainHash).orElse("");
        String chainHash = computeChainHash(eventHash, previousHash);

        AuditHash hash = AuditHash.builder()
                .auditLogId(savedLog.getId())
                .tenantId(saved.getTenantId())
                .sequenceNumber(sequenceNumber)
                .eventHash(eventHash)
                .previousHash(previousHash)
                .chainHash(chainHash)
                .validChain(true)
                .build();

        auditHashRepository.save(hash);
        log.info("Audit event ingested and hash-chained: eventId={}", saved.getId());
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditEvents(UUID tenantId, LocalDateTime since, Pageable pageable) {
        return auditEventRepository.findByTenantSince(tenantId, since, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(UUID tenantId, LocalDateTime since, Pageable pageable) {
        return auditLogRepository.findActive(tenantId, since, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getEntityAuditTrail(UUID tenantId, String entityType, UUID entityId) {
        return auditEventRepository.findByEntity(tenantId, entityType, entityId);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> getEventsByService(UUID tenantId, String sourceService, LocalDateTime since, Pageable pageable) {
        return auditEventRepository.findBySourceService(tenantId, sourceService, since, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditHash> getHashChain(UUID tenantId, UUID logId) {
        return auditHashRepository.findChainForLog(logId, tenantId);
    }

    @Transactional(readOnly = true)
    public boolean validateHashChain(UUID tenantId, UUID logId) {
        List<AuditHash> chain = auditHashRepository.findChainForLog(logId, tenantId);

        if (chain.isEmpty()) {
            return false;
        }

        String previousHash = "";
        for (AuditHash hash : chain) {
            String expectedChainHash = computeChainHash(hash.getEventHash(), previousHash);
            if (!expectedChainHash.equals(hash.getChainHash())) {
                log.error("Hash chain validation failed at sequence {}", hash.getSequenceNumber());
                hash.setValidChain(false);
                auditHashRepository.save(hash);
                return false;
            }
            previousHash = hash.getChainHash();
        }

        return true;
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getArchivedLogs(UUID tenantId, LocalDateTime since, Pageable pageable) {
        return auditLogRepository.findArchived(tenantId, since, pageable);
    }

    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash computation failed", e);
        }
    }

    private String computeChainHash(String eventHash, String previousHash) {
        return computeHash(eventHash + previousHash);
    }
}
