package io.datasheild.notificationservice.service;

import io.datasheild.notificationservice.channel.*;
import io.datasheild.notificationservice.dto.TriggerNotificationRequest;
import io.datasheild.notificationservice.entity.*;
import io.datasheild.notificationservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateLocalizationRepository localizationRepository;
    private final NotificationEventRepository eventRepository;
    private final NotificationLogRepository logRepository;
    private final DeadLetterQueueRepository dlqRepository;
    private final EmailChannelService emailChannel;
    private final SMSChannelService smsChannel;
    private final InAppChannelService inappChannel;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public NotificationEvent triggerNotification(UUID tenantId, TriggerNotificationRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid notification request");
        }

        log.info("Triggering notification: correlation={} event={}", request.getCorrelationId(), request.getEventType());

        // Load template
        NotificationTemplate template = templateRepository.findActiveByTenantAndCode(tenantId, request.getTemplateCode())
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + request.getTemplateCode()));

        // Load localization
        String language = request.getLanguage() != null ? request.getLanguage() : "en";
        TemplateLocalization localization = localizationRepository.findByTemplateAndLanguage(template.getId(), language)
                .orElseGet(() -> localizationRepository.findByTemplateAndLanguage(template.getId(), "en")
                        .orElseThrow(() -> new IllegalArgumentException("Template localization not found")));

        // Create event
        NotificationEvent event = NotificationEvent.builder()
                .tenantId(tenantId)
                .correlationId(request.getCorrelationId())
                .eventType(NotificationEvent.EventType.NOTIFICATION_TRIGGERED)
                .eventPayload(serializePayload(request))
                .status(NotificationEvent.EventStatus.PENDING)
                .retryCount(0)
                .build();

        NotificationEvent savedEvent = eventRepository.save(event);
        log.info("Created notification event: id={}", savedEvent.getId());

        // Send asynchronously
        sendNotificationAsync(tenantId, savedEvent, template, localization, request);

        return savedEvent;
    }

    @Async
    @Transactional
    public void sendNotificationAsync(UUID tenantId, NotificationEvent event, NotificationTemplate template, TemplateLocalization localization, TriggerNotificationRequest request) {
        try {
            event.setStatus(NotificationEvent.EventStatus.PROCESSING);
            eventRepository.save(event);

            int successCount = 0;
            for (String recipient : request.getRecipients()) {
                for (String channelName : request.getChannels()) {
                    try {
                        NotificationLog log = sendViaChannel(tenantId, channelName, recipient, localization.getSubject(), localization.getBody(), request.getVariables(), event.getId());
                        if (log.getDeliveryStatus() == NotificationLog.DeliveryStatus.DELIVERED || log.getDeliveryStatus() == NotificationLog.DeliveryStatus.SENT) {
                            successCount++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to send via channel: {} recipient: {} error: {}", channelName, recipient, e.getMessage());
                        handleDeliveryFailure(tenantId, event, e);
                    }
                }
            }

            event.setStatus(successCount > 0 ? NotificationEvent.EventStatus.SENT : NotificationEvent.EventStatus.FAILED);
            eventRepository.save(event);

        } catch (Exception e) {
            log.error("Async send failed for event: {} error: {}", event.getId(), e.getMessage());
            event.setStatus(NotificationEvent.EventStatus.FAILED);
            eventRepository.save(event);
            handleDeliveryFailure(tenantId, event, e);
        }
    }

    private NotificationLog sendViaChannel(UUID tenantId, String channelName, String recipient, String subject, String body, Map<String, String> variables, UUID eventId) {
        return switch (channelName.toUpperCase()) {
            case "EMAIL" -> emailChannel.send(recipient, subject, body, variables, eventId, tenantId);
            case "SMS" -> smsChannel.send(recipient, subject, body, variables, eventId, tenantId);
            case "INAPP" -> inappChannel.send(recipient, subject, body, variables, eventId, tenantId);
            default -> throw new IllegalArgumentException("Unknown channel: " + channelName);
        };
    }

    @Transactional
    public void handleDeliveryFailure(UUID tenantId, NotificationEvent event, Exception e) {
        DeadLetterQueue dlq = DeadLetterQueue.builder()
                .eventId(event.getId())
                .tenantId(tenantId)
                .originalPayload(event.getEventPayload())
                .failureReason(e.getMessage())
                .retryCount(event.getRetryCount())
                .status(DeadLetterQueue.DLQStatus.PENDING)
                .build();

        dlqRepository.save(dlq);
        log.error("Event moved to DLQ: eventId={} reason={}", event.getId(), e.getMessage());
    }

    @Transactional(readOnly = true)
    public Page<NotificationLog> getNotificationLogs(UUID tenantId, LocalDateTime since, Pageable pageable) {
        return logRepository.findByStatus(tenantId, NotificationLog.DeliveryStatus.DELIVERED, since, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DeadLetterQueue> getDLQItems(UUID tenantId, LocalDateTime since, Pageable pageable) {
        return dlqRepository.findByStatus(tenantId, DeadLetterQueue.DLQStatus.PENDING, since, pageable);
    }

    @Transactional
    public void resolveDLQItem(UUID tenantId, UUID dlqId, String resolution) {
        DeadLetterQueue dlq = dlqRepository.findById(dlqId)
                .orElseThrow(() -> new IllegalArgumentException("DLQ item not found"));

        dlq.setStatus(DeadLetterQueue.DLQStatus.RESOLVED);
        dlq.setResolvedAt(LocalDateTime.now());
        dlqRepository.save(dlq);

        log.info("DLQ item resolved: id={} resolution={}", dlqId, resolution);
    }

    @Cacheable(value = "templates", key = "#tenantId.toString()")
    public List<NotificationTemplate> getTemplates(UUID tenantId) {
        return templateRepository.findAllActive(tenantId);
    }

    private String serializePayload(TriggerNotificationRequest request) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request);
        } catch (Exception e) {
            return "{}";
        }
    }
}
