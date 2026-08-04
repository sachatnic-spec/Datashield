package io.datasheild.auditservice.controller;

import io.datasheild.auditservice.dto.AuditEventResponse;
import io.datasheild.auditservice.dto.AuditLogResponse;
import io.datasheild.auditservice.entity.AuditEvent;
import io.datasheild.auditservice.entity.AuditHash;
import io.datasheild.auditservice.entity.AuditLog;
import io.datasheild.auditservice.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit", description = "Centralized immutable audit trail")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logs")
    @Operation(summary = "Get audit logs", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(defaultValue = "7") int daysSince,
            Pageable pageable) {

        log.info("GET /v1/audit/logs tenantId={}", tenantId);

        LocalDateTime since = LocalDateTime.now().minusDays(daysSince);
        Page<AuditLog> logs = auditService.getAuditLogs(tenantId, since, pageable);

        Page<AuditLogResponse> responses = logs.map(log -> AuditLogResponse.builder()
                .id(log.getId())
                .auditEventId(log.getAuditEventId())
                .eventSummary(log.getEventSummary())
                .s3ObjectKey(log.getS3ObjectKey())
                .sha256Hash(log.getSha256Hash())
                .hashChainValid(log.getHashChainValid())
                .archived(log.getArchived())
                .createdAt(log.getCreatedAt())
                .archivedAt(log.getArchivedAt())
                .build());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/events")
    @Operation(summary = "Get audit events", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Page<AuditEventResponse>> getAuditEvents(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(defaultValue = "7") int daysSince,
            Pageable pageable) {

        log.info("GET /v1/audit/events tenantId={}", tenantId);

        LocalDateTime since = LocalDateTime.now().minusDays(daysSince);
        Page<AuditEvent> events = auditService.getAuditEvents(tenantId, since, pageable);

        Page<AuditEventResponse> responses = events.map(event -> AuditEventResponse.builder()
                .id(event.getId())
                .correlationId(event.getCorrelationId())
                .sourceService(event.getSourceService())
                .entityType(event.getEntityType())
                .eventType(event.getEventType())
                .entityId(event.getEntityId())
                .actorId(event.getActorId())
                .actorRole(event.getActorRole())
                .ipAddress(event.getIpAddress())
                .createdAt(event.getCreatedAt())
                .previousState(event.getPreviousState())
                .currentState(event.getCurrentState())
                .build());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/trail/{entityType}/{entityId}")
    @Operation(summary = "Get entity audit trail", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<List<AuditEventResponse>> getEntityTrail(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable String entityType,
            @PathVariable UUID entityId) {

        log.info("GET /v1/audit/trail/{}/{} tenantId={}", entityType, entityId, tenantId);

        List<AuditEvent> events = auditService.getEntityAuditTrail(tenantId, entityType, entityId);

        List<AuditEventResponse> responses = events.stream()
                .map(event -> AuditEventResponse.builder()
                        .id(event.getId())
                        .correlationId(event.getCorrelationId())
                        .sourceService(event.getSourceService())
                        .entityType(event.getEntityType())
                        .eventType(event.getEventType())
                        .entityId(event.getEntityId())
                        .actorId(event.getActorId())
                        .actorRole(event.getActorRole())
                        .ipAddress(event.getIpAddress())
                        .createdAt(event.getCreatedAt())
                        .previousState(event.getPreviousState())
                        .currentState(event.getCurrentState())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/hash-chain/{logId}")
    @Operation(summary = "Get hash chain for audit log", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<java.util.Map<String, Object>> getHashChain(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID logId) {

        log.info("GET /v1/audit/hash-chain/{} tenantId={}", logId, tenantId);

        List<AuditHash> chain = auditService.getHashChain(tenantId, logId);
        boolean isValid = auditService.validateHashChain(tenantId, logId);

        return ResponseEntity.ok(java.util.Map.of(
                "log_id", logId,
                "chain_length", chain.size(),
                "valid", isValid,
                "hashes", chain
        ));
    }

    @PostMapping("/verify-chain/{logId}")
    @Operation(summary = "Verify immutability of audit log", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<java.util.Map<String, Object>> verifyChain(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID logId) {

        log.info("POST /v1/audit/verify-chain/{} tenantId={}", logId, tenantId);

        boolean isValid = auditService.validateHashChain(tenantId, logId);

        return ResponseEntity.ok(java.util.Map.of(
                "log_id", logId,
                "immutable", isValid,
                "timestamp", LocalDateTime.now()
        ));
    }
}
