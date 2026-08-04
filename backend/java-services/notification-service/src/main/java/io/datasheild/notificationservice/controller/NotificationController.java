package io.datasheild.notificationservice.controller;

import io.datasheild.notificationservice.dto.DLQItemResponse;
import io.datasheild.notificationservice.dto.NotificationResponse;
import io.datasheild.notificationservice.dto.TriggerNotificationRequest;
import io.datasheild.notificationservice.entity.DeadLetterQueue;
import io.datasheild.notificationservice.entity.NotificationEvent;
import io.datasheild.notificationservice.entity.NotificationLog;
import io.datasheild.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Multi-channel notification dispatcher")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/trigger")
    @Operation(summary = "Trigger multi-channel notification", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<NotificationResponse> triggerNotification(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestBody TriggerNotificationRequest request) {

        log.info("POST /v1/notifications/trigger tenantId={}", tenantId);

        NotificationEvent event = notificationService.triggerNotification(tenantId, request);

        NotificationResponse response = NotificationResponse.builder()
                .eventId(event.getId())
                .correlationId(event.getCorrelationId())
                .status(event.getStatus().name())
                .recipientCount(0)
                .createdAt(event.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/logs")
    @Operation(summary = "Get notification delivery logs", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Page<NotificationLog>> getNotificationLogs(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(defaultValue = "7") int daysSince,
            Pageable pageable) {

        log.info("GET /v1/notifications/logs tenantId={}", tenantId);

        LocalDateTime since = LocalDateTime.now().minusDays(daysSince);
        Page<NotificationLog> logs = notificationService.getNotificationLogs(tenantId, since, pageable);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/dlq")
    @Operation(summary = "Get dead-letter queue items", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Page<DLQItemResponse>> getDLQItems(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(defaultValue = "7") int daysSince,
            Pageable pageable) {

        log.info("GET /v1/notifications/dlq tenantId={}", tenantId);

        LocalDateTime since = LocalDateTime.now().minusDays(daysSince);
        Page<DeadLetterQueue> dlqItems = notificationService.getDLQItems(tenantId, since, pageable);

        Page<DLQItemResponse> responses = dlqItems.map(item -> DLQItemResponse.builder()
                .id(item.getId())
                .eventId(item.getEventId())
                .failureReason(item.getFailureReason())
                .retryCount(item.getRetryCount())
                .status(item.getStatus().name())
                .createdAt(item.getCreatedAt())
                .build());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/dlq/{dlqId}/resolve")
    @Operation(summary = "Resolve dead-letter queue item", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<Void> resolveDLQItem(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID dlqId,
            @RequestParam String resolution) {

        log.info("PUT /v1/notifications/dlq/{}/resolve tenantId={}", dlqId, tenantId);

        notificationService.resolveDLQItem(tenantId, dlqId, resolution);

        return ResponseEntity.ok().build();
    }
}
